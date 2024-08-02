pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting Build Stage...'
                echo 'Building...'
                sh 'ls' // This will run the 'ls' command
            }
        }
    }
}

