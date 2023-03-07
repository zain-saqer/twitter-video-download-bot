resource "aws_iam_role" "iam_for_twtr_sqs_lambda" {
  name = "${local.prefix}-sqs-lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "tweet_sqs" {
  policy_arn = aws_iam_policy.tweet_sqs.arn
  role = aws_iam_role.iam_for_twtr_sqs_lambda.name
}

resource "aws_iam_policy" "tweet_sqs" {
  policy = data.aws_iam_policy_document.tweet_sqs.json
}

data "aws_iam_policy_document" "tweet_sqs" {
  statement {
    sid       = "AllowSQSPermissions"
    effect    = "Allow"
    resources = ["*"]

    actions = [
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ReceiveMessage",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:GetItem",
      "dynamodb:Scan",
      "dynamodb:Query",
      "dynamodb:UpdateItem",
    ]
  }

  statement {
    sid       = "AllowInvokingLambdas"
    effect    = "Allow"
    resources = ["arn:aws:lambda:*:*:function:*"]
    actions   = ["lambda:InvokeFunction"]
  }

  statement {
    sid       = "AllowCreatingLogGroups"
    effect    = "Allow"
    resources = ["arn:aws:logs:*:*:*"]
    actions   = ["logs:CreateLogGroup"]
  }
  statement {
    sid       = "AllowWritingLogs"
    effect    = "Allow"
    resources = ["arn:aws:logs:*:*:log-group:/aws/lambda/*:*"]

    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
  }
}

resource "aws_lambda_function" "tweet_sqs" {
  runtime          = var.lambda_runtime
  filename         = var.lambda_payload_filename
  source_code_hash = filebase64sha256(var.lambda_payload_filename)
  function_name    = "${local.prefix}-tweet-sqs"

  environment {
    variables = local.oauth20_redirection_lambda_environment_vars
  }

  handler          = "de.saqer.twittervideodownloadbot.worker.aws.lambda.TweetSQSHandler::handleRequest"
  timeout          = 60
  memory_size      = 512
  role             = aws_iam_role.iam_for_twtr_sqs_lambda.arn
}

resource "aws_lambda_event_source_mapping" "tweet_sqs" {
  event_source_arn = aws_sqs_queue.twitter_video_download_requests_queue.arn
  enabled          = true
  function_name    = aws_lambda_function.tweet_sqs.arn
  batch_size       = 1
}

resource "aws_cloudwatch_log_group" "tweet_sqs" {
  name              = "/aws/lambda/${aws_lambda_function.tweet_sqs.function_name}"
  retention_in_days = 7
  lifecycle {
    prevent_destroy = false
  }
}
