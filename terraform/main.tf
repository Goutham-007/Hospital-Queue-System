# Terraform configuration for Hospital Queue System
# This defines what the AWS infrastructure would look like
# No AWS account needed to show this file in interview

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-south-1"  # Mumbai region
}

# EC2 instance for the application
resource "aws_instance" "hospital_queue_server" {
  ami           = "ami-0f58b397bc5c1f2e8"
  instance_type = "t2.micro"  # Free tier eligible

  tags = {
    Name    = "HospitalQueueServer"
    Project = "HospitalQueueManagement"
  }
}

# Security group - controls network access
resource "aws_security_group" "app_sg" {
  name        = "hospital-queue-sg"
  description = "Security group for Hospital Queue application"

  # Allow backend API access
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow SSH for management
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Allow all outbound
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}