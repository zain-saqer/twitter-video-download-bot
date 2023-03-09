variable "region" {
  # set aws region
  default = "us-east-1"
}

variable "shared_credentials_files" {
  default = ["$HOME/.aws/credentials"]
}

variable "lambda_payload_filename" {
  default = "build/distributions/TwitterVideoDownloadBot-1.0-SNAPSHOT.zip"
}

variable "stream_payload_filename" {
  default = "TwitterVideoDownloadBot-1.0-SNAPSHOT.jar"
}

variable "stream_payload_filename_path" {
  default = "build/libs/TwitterVideoDownloadBot-1.0-SNAPSHOT.jar"
}

variable "lambda_runtime" {
  default = "java11"
}

variable "api_env_stage_name" {
  default = "v1"
}

variable private_key_path {
  description = "Path to the SSH private key to be used for authentication"
  default = "~/.ssh/id_ed25519"
}

variable public_key_path {
  description = "Path to the SSH public key to be used for authentication"
  default = "~/.ssh/id_ed25519.pub"
}
variable twitter_oauth20_credentials_table_name {
  default = "TWITTER_OAUTH20_CREDENTIALS"
}

locals {
  user_arn = "arn:aws:iam::640779667316:user/twitter-video-download-bot"
  prefix = "twtr-vid-down-bot"
  oauth20_redirection_lambda_environment_vars =  {
    TWITTER_OAUTH20_CREDENTIALS_DYNAMODB_TABLE = var.twitter_oauth20_credentials_table_name
  }
}