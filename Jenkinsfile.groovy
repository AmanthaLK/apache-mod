pipeline {
    agent any

    stages {
        stage('Test SSH Connection and Access Root') {
            steps {
                script {
                    sshagent(['aws-personal']) {
                        try {
                            sh '''
                                echo "Testing SSH connection..."
                                ssh -o StrictHostKeyChecking=no ubuntu@172.31.82.42 << 'EOF'
                                echo "Logged in as $(whoami)"
                                sudo -i bash -c '
                                    echo "Switched to root";
                                    cd /etc;
                                    ls;
                                '
                                exit
                                EOF
                            '''
                        } catch (Exception e) {
                            echo "SSH connection or root access failed: ${e.message}"
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
        }
    }
}
