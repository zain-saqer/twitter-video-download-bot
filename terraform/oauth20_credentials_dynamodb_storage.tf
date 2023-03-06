resource "aws_dynamodb_table" "twitter_oauth20_credentials_dynamodb_table" {
  name = var.twitter_oauth20_credentials_table_name
  billing_mode = "PROVISIONED"
  read_capacity= "30"
  write_capacity= "30"
  hash_key = "ID"
  attribute {
    name = "ID"
    type = "S"
  }
}