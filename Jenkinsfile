// ============================================================
//  JENKINSFILE — Pipeline CI/CD Java / Maven
//  Application de démonstration pour exposé Jenkins
//  Auteur : Démo Jenkins
// ============================================================

pipeline {

    // -------------------------------------------------------
    // AGENT : où Jenkins va exécuter le pipeline
    // Ici on utilise Docker pour isoler l'environnement
    // -------------------------------------------------------
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'   // Image Maven + JDK 17
            args  '-v $HOME/.m2:/root/.m2'            // Cache Maven local (plus rapide)
        }
    }

    // -------------------------------------------------------
    // VARIABLES D'ENVIRONNEMENT
    // -------------------------------------------------------
    environment {
        APP_NAME      = 'demo-java-app'
        APP_VERSION   = '1.0.0'
        DOCKER_IMAGE  = "monrepo/${APP_NAME}:${APP_VERSION}"
        SONAR_TOKEN   = credentials('sonarqube-token')   // Secret stocké dans Jenkins
    }

    // -------------------------------------------------------
    // OPTIONS GLOBALES DU PIPELINE
    // -------------------------------------------------------
    options {
        timestamps()                    // Affiche l'heure dans les logs
        timeout(time: 30, unit: 'MINUTES')  // Arrête le build si > 30 min
        buildDiscarder(logRotator(numToKeepStr: '10'))  // Garde les 10 derniers builds
        disableConcurrentBuilds()       // Empêche 2 builds en parallèle
    }

    // -------------------------------------------------------
    // DÉCLENCHEURS AUTOMATIQUES
    // -------------------------------------------------------
    triggers {
        // Vérifie le dépôt Git toutes les 5 minutes (polling)
        // En production, préférez un webhook GitHub/GitLab
        pollSCM('H/5 * * * *')
    }

    // -------------------------------------------------------
    // PARAMÈTRES (optionnel — permet de personnaliser le build)
    // -------------------------------------------------------
    parameters {
        booleanParam(
            name: 'DEPLOY_TO_STAGING',
            defaultValue: true,
            description: 'Déployer sur l\'environnement de staging ?'
        )
        booleanParam(
            name: 'RUN_SONAR',
            defaultValue: true,
            description: 'Lancer l\'analyse SonarQube ?'
        )
        choice(
            name: 'ENV_TARGET',
            choices: ['staging', 'production'],
            description: 'Environnement cible du déploiement'
        )
    }

    // ============================================================
    //  STAGES — Les étapes du pipeline
    // ============================================================
    stages {

        // ---------------------------------------------------
        // STAGE 1 : Récupération du code source
        // ---------------------------------------------------
        stage('Checkout') {
            steps {
                echo '📥 Récupération du code source depuis Git...'
                checkout scm   // Récupère automatiquement le dépôt configuré dans le job
                sh 'echo "Branche : $(git branch --show-current)"'
                sh 'echo "Dernier commit : $(git log -1 --oneline)"'
            }
        }

        // ---------------------------------------------------
        // STAGE 2 : Compilation
        // ---------------------------------------------------
        stage('Build') {
            steps {
                echo '⚙️  Compilation du projet Maven...'
                sh 'mvn clean compile -B'
                // -B = mode batch (pas de couleurs, pour les logs Jenkins)
            }
            post {
                failure {
                    echo '❌ Échec de la compilation ! Vérifiez les erreurs ci-dessus.'
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 3 : Tests unitaires
        // ---------------------------------------------------
        stage('Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                sh 'mvn test -B'
            }
            post {
                always {
                    // Publie les résultats des tests dans l'interface Jenkins
                    junit '**/target/surefire-reports/*.xml'
                    echo '📊 Rapport de tests publié.'
                }
                failure {
                    echo '❌ Des tests ont échoué ! Consultez le rapport JUnit.'
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 4 : Tests d'intégration
        // ---------------------------------------------------
        stage('Tests Integration') {
            steps {
                echo '🔗 Exécution des tests d\'intégration...'
                sh 'mvn verify -B -DskipUnitTests=true'
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 5 : Analyse qualité du code (SonarQube)
        // Exécuté uniquement si le paramètre RUN_SONAR est true
        // ---------------------------------------------------
        stage('Analyse SonarQube') {
            when {
                expression { params.RUN_SONAR == true }
            }
            steps {
                echo '🔍 Analyse de la qualité du code avec SonarQube...'
                sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${APP_NAME} \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.login=${SONAR_TOKEN}
                """
            }
        }

        // ---------------------------------------------------
        // STAGE 6 : Packaging (création du JAR)
        // ---------------------------------------------------
        stage('Package') {
            steps {
                echo '📦 Création du JAR...'
                sh 'mvn package -B -DskipTests'
                // Archive l'artefact dans Jenkins
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                echo "✅ Artefact archivé : target/${APP_NAME}-${APP_VERSION}.jar"
            }
        }

        // ---------------------------------------------------
        // STAGE 7 : Construction de l'image Docker
        // ---------------------------------------------------
        stage('Build Image Docker') {
            steps {
                echo '🐳 Construction de l\'image Docker...'
                sh "docker build -t ${DOCKER_IMAGE} ."
                echo "✅ Image créée : ${DOCKER_IMAGE}"
            }
        }

        // ---------------------------------------------------
        // STAGE 8 : Déploiement sur Staging
        // Exécuté uniquement si DEPLOY_TO_STAGING est true
        // ET uniquement sur la branche main/develop
        // ---------------------------------------------------
        stage('Deploy Staging') {
            when {
                allOf {
                    expression { params.DEPLOY_TO_STAGING == true }
                    anyOf {
                        branch 'main'
                        branch 'develop'
                    }
                }
            }
            steps {
                echo '🚀 Déploiement sur l\'environnement de staging...'
                sh """
                    docker stop ${APP_NAME}-staging || true
                    docker rm   ${APP_NAME}-staging || true
                    docker run -d \
                        --name ${APP_NAME}-staging \
                        -p 8081:8080 \
                        --env SPRING_PROFILES_ACTIVE=staging \
                        ${DOCKER_IMAGE}
                """
                echo '✅ Application déployée sur http://staging:8081'
            }
        }

        // ---------------------------------------------------
        // STAGE 9 : Tests de fumée post-déploiement
        // Vérifie que l'application répond correctement
        // ---------------------------------------------------
        stage('Smoke Tests') {
            when {
                expression { params.DEPLOY_TO_STAGING == true }
            }
            steps {
                echo '💨 Tests de fumée...'
                // Attend que l'application démarre (30 secondes max)
                sh '''
                    for i in $(seq 1 6); do
                        if curl -sf http://staging:8081/actuator/health; then
                            echo "✅ Application UP !"
                            exit 0
                        fi
                        echo "⏳ Attente démarrage... ($i/6)"
                        sleep 5
                    done
                    echo "❌ L'application ne répond pas."
                    exit 1
                '''
            }
        }

    }
    // ============================================================
    // FIN DES STAGES
    // ============================================================

    // ============================================================
    //  POST — Actions après le pipeline (succès, échec, toujours)
    // ============================================================
    post {

        success {
            echo '🎉 Pipeline terminé avec SUCCÈS !'
            // Notification Slack (nécessite le plugin Slack Notification)
            slackSend(
                channel: '#ci-cd',
                color: 'good',
                message: "✅ *${APP_NAME}* v${APP_VERSION} — Build #${BUILD_NUMBER} réussi ! (<${BUILD_URL}|Voir le build>)"
            )
        }

        failure {
            echo '💥 Pipeline ÉCHOUÉ !'
            slackSend(
                channel: '#ci-cd',
                color: 'danger',
                message: "❌ *${APP_NAME}* v${APP_VERSION} — Build #${BUILD_NUMBER} en échec ! (<${BUILD_URL}|Voir les logs>)"
            )
            // Envoie aussi un email à l'équipe
            emailext(
                subject: "[JENKINS] ❌ Build échoué — ${APP_NAME} #${BUILD_NUMBER}",
                body: "Le build ${BUILD_NUMBER} a échoué. Consultez les logs : ${BUILD_URL}",
                to: 'equipe@monentreprise.com'
            )
        }

        always {
            echo '🧹 Nettoyage de l\'espace de travail...'
            cleanWs()   // Supprime les fichiers temporaires après le build
        }

    }

}
