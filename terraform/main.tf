provider "aws" {
  shared_credentials_files = var.shared_credentials_files
  region = var.region
}