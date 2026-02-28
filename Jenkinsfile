// ============================================================
//  JENKINSFILE — Pipeline CI/CD Java / Maven
//  Version simplifiée pour démo (sans Docker agent)
// ============================================================

pipeline {

    // ✅ "any" = Jenkins utilise son propre environnement
    // Pas besoin du plugin Docker
    agent any

    // -------------------------------------------------------
    // VARIABLES D'ENVIRONNEMENT
    //Test
    // -------------------------------------------------------
    environment {
        APP_NAME    = 'demo-java-app'
        APP_VERSION = '1.0.0'
    }

    // -------------------------------------------------------
    // OPTIONS GLOBALES
    // -------------------------------------------------------
    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    // ============================================================
    //  STAGES
    // ============================================================
    stages {

        // ---------------------------------------------------
        // STAGE 1 : Récupération du code source
        // ---------------------------------------------------
        stage('Checkout') {
            steps {
                echo '📥 Récupération du code source depuis Git...'
                checkout scm
            }
        }

        // ---------------------------------------------------
        // STAGE 2 : Compilation
        // ---------------------------------------------------
        stage('Build') {
            steps {
                echo '⚙️  Compilation du projet Maven...'
                bat 'mvn clean compile -B'
            }
            post {
                failure {
                    echo '❌ Échec de la compilation !'
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 3 : Tests unitaires
        // ---------------------------------------------------
        stage('Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                bat 'mvn test -B'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    echo '📊 Rapport de tests publié.'
                }
                failure {
                    echo '❌ Des tests ont échoué !'
                }
            }
        }

        // ---------------------------------------------------
        // STAGE 4 : Packaging (création du JAR)
        // ---------------------------------------------------
        stage('Package') {
            steps {
                echo '📦 Création du JAR...'
                bat 'mvn package -B -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                echo "✅ JAR archivé avec succès !"
            }
        }

    }

    // ============================================================
    //  POST
    // ============================================================
    post {
        success {
            echo '🎉 Pipeline terminé avec SUCCÈS !'
        }
        failure {
            echo '💥 Pipeline ÉCHOUÉ ! Consultez les logs ci-dessus.'
        }
        always {
            echo '🧹 Fin du pipeline.'
            cleanWs()
        }
    }

}