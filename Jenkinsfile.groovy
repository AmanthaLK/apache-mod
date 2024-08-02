pipeline {
    agent any

    stages {
        stage('Test SSH Connection') {
            steps {
                script {
                    sshagent(['aws-personal']) {
                        sh '''
                            echo "Testing SSH connection..."
                            ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 "echo Connection successful"
                        '''
                    }
                }
            }
        }
    }
}
