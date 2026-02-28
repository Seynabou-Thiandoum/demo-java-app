pipeline {

    agent any

    environment {
        APP_NAME    = 'demo-java-app'
        APP_VERSION = '1.0.0'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                echo '📥 Récupération du code source depuis Git...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '⚙️  Compilation du projet Maven...'
                sh 'mvn clean compile -B'
            }
            post {
                failure {
                    echo '❌ Échec de la compilation !'
                }
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                sh 'mvn test -B'
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

        stage('Package') {
            steps {
                echo '📦 Création du JAR...'
                sh 'mvn package -B -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                echo '✅ JAR archivé avec succès !'
            }
        }

    }

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