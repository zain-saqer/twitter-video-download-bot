resource "aws_iam_role" "iam_role_for_oauth20_redirection_lambda" {
  name = "${local.prefix}-oauth20-redirection-lambda-invoke-role"
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

resource "aws_iam_policy" "iam_policy_for_oauth20_redirection_lambda" {
  name = "lambda-invoke-policy"
  path = "/"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "LambdaPolicy",
        "Effect": "Allow",
        "Action": [
          "logs:CreateLogStream",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem",
          "dynamodb:GetItem",
          "dynamodb:Scan",
          "dynamodb:Query",
          "dynamodb:UpdateItem",
          "logs:CreateLogGroup",
          "logs:PutLogEvents"
        ],
        "Resource": "*"
      }
    ]
  }
EOF
}

resource "aws_iam_role_policy_attachment" "aws_iam_role_oauth20_redirection_policy_attachment" {
  role       = aws_iam_role.iam_role_for_oauth20_redirection_lambda.name
  policy_arn = aws_iam_policy.iam_policy_for_oauth20_redirection_lambda.arn
}

resource "aws_lambda_function" "oauth20_redirection_lambda_function" {
  runtime          = var.lambda_runtime
  filename         = var.lambda_payload_filename
  source_code_hash = filebase64sha256(var.lambda_payload_filename)
  function_name    = "${local.prefix}-oauth20-redirection"

  environment {
    variables = local.oauth20_redirection_lambda_environment_vars
  }

  handler          = "de.saqer.twittervideodownloadbot.twitter.oauth2.awslambdahandler.CallbackHandler::handleRequest"
  timeout          = 60
  memory_size      = 256
  role             = aws_iam_role.iam_role_for_oauth20_redirection_lambda.arn
}

resource "aws_cloudwatch_log_group" "oauth20_redirection_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.oauth20_redirection_lambda_function.function_name}"
  retention_in_days = 7
  lifecycle {
    prevent_destroy = false
  }
}

resource "aws_apigatewayv2_integration" "gw_integration_redirection" {
  api_id = aws_apigatewayv2_api.gw_v2_api.id

  integration_uri    = aws_lambda_function.oauth20_redirection_lambda_function.invoke_arn
  integration_type   = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_route" "gw_route_redirection" {
  api_id = aws_apigatewayv2_api.gw_v2_api.id

  route_key = "GET /redirection"
  target    = "integrations/${aws_apigatewayv2_integration.gw_integration_redirection.id}"
}

resource "aws_lambda_permission" "api_gw_redirection" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.oauth20_redirection_lambda_function.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.gw_v2_api.execution_arn}/*/*"
}