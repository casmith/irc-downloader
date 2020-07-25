pipeline {
    agent none

    environment {
        USERNAME = credentials('marvin-github-username');
        TOKEN = credentials('marvin-github-token')
        registry = 'casmith/marvin'
        registryCredential = 'dockerhub'
    }

    stages {
        stage('build') {
            agent { docker { image 'gradle:6.3.0-jdk11' } }
            steps {
                sh 'gradle dist'
            }
        }
        stage('publish image') {
            agent any
            steps {
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                    docker.withRegistry( '', registryCredential ) {
                        dockerImage.push()
                    }
                    sh "docker rmi $registry:$BUILD_NUMBER"
                }
            }
        }
    }
}
