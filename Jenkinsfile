pipeline {
    agent { docker { image 'gradle:6.3.0-jdk11' } }

    environment {
        USERNAME = credentials('marvin-github-username');
        TOKEN = credentials('marvin-github-token')
        registry = 'casmith/marvin'
        registryCredential = 'dockerhub'
    }

    stages {
        stage('build') {
            steps {
                sh 'gradle dist'
            }
        }
        stage('docker docker image') {
            steps {
                script {
                    docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }
    }
}
