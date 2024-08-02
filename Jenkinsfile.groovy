pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting Build Stage...'
                echo 'Building...'
                // Add your build steps here
            }
        }

        stage('Test') {
            steps {
                echo 'Starting Test Stage...'
                echo 'Testing...'
                // Add your test steps here
            }
        }

        stage('Deploy') {
            steps {
                echo 'Starting Deploy Stage...'
                echo 'Deploying...'
                // Add your deploy steps here
            }
        }

        stage('Hello World') {
            steps {
                script {
                    def message = 'Hello, World!'
                    echo message
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
        }
    }
}
