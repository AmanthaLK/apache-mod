pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Starting Build Stage...'
                echo 'Building...'
                sh 'ls'                // List files and directories
                sh 'pwd'               // Print the current working directory
                sh 'cat index.html'    // Display the contents of index.html
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs() // Clean the workspace at the end of the pipeline
            }
        }
    }
}
