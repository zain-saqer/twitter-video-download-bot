resource "aws_sqs_queue" "twitter_video_download_requests_queue_deadletter" {
  name = "twitter-video-download-requests-deadletter-queue"
}

resource "aws_sqs_queue" "twitter_video_download_requests_queue" {
  name                      = "twitter-video-download-requests"
  visibility_timeout_seconds = 30
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 345600
  receive_wait_time_seconds = 20
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.twitter_video_download_requests_queue_deadletter.arn
    maxReceiveCount     = 4
  })
}

resource "aws_sqs_queue_policy" "twitter_video_download_requests_policy" {
  queue_url = aws_sqs_queue.twitter_video_download_requests_queue.id

  policy = <<POLICY
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "${local.user_arn}"
      },
      "Action": "sqs:*",
      "Resource": "${aws_sqs_queue.twitter_video_download_requests_queue.arn}"
    }
  ]
}
POLICY
}