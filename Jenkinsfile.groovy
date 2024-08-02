pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting Build Stage...'
                echo 'Building...'
                sh 'ls' // List files and directories in the current directory
                sh 'cat index.html' // Display the contents of index.html
            }
        }
        stage('List Jenkins Master Directory') {
            steps {
                echo 'Listing contents of /home/ubuntu on Jenkins master...'
                echo 'test'
                
            }
        }
    }
}
