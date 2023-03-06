##resource "aws_vpc" "default" {
##  cidr_block = "10.0.0.0/16"
##}
#
#resource "aws_security_group" "twitter_filtered_stream_security_group" {
#  name        = "twitter-filtered-stream-security-group"
#  description = "Twitter filtered stream security group"
#  # vpc_id      = "${aws_vpc.default.id}"
#
#  # SSH access from anywhere
#  ingress {
#    from_port   = 22
#    to_port     = 22
#    protocol    = "tcp"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
#
#  # HTTP access from the internet
#  egress {
#    from_port   = 80
#    to_port     = 80
#    protocol    = "tcp"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
#
#  # outbound internet access
#  egress {
#    from_port   = 0
#    to_port     = 0
#    protocol    = "-1"
#    cidr_blocks = ["0.0.0.0/0"]
#  }
#}
#
#resource "aws_key_pair" "aws_18" {
#  key_name   = "aws_18"               # key pair name AWS
#  public_key = file(var.public_key_path)
#}
#
#resource "aws_instance" "twitter_filtered_stream_ec2_instance" {
#  ami = "ami-060cde69"
#  instance_type = "t2.micro"
#
#  key_name = aws_key_pair.aws_18.id
#
#  vpc_security_group_ids = [aws_security_group.twitter_filtered_stream_security_group.id]
#
#  user_data = "java -jar ${var.stream_payload_filename}"
#
#  connection {
#    # The default username for our AMI
#    user        = "ubuntu"
#    type        = "ssh"
#    private_key = file(var.private_key_path)
#    host        = self.public_ip
#  }
#
#  # install java, create dir
#  provisioner "remote-exec" {
#    inline = [
#      "sudo apt -y update",
#      "sudo apt -y install openjdk-9-jre-headless",
#    ]
#  }
#
#  # upload jar file
#  provisioner "file" {
#    source      = var.stream_payload_filename_path
#    destination = "/home/ubuntu/${var.stream_payload_filename}"
#  }
#
##  # download logFile results
##  provisioner "local-exec" {
##    command = "sftp -b ${var.sftp_batch_path} -i ${var.private_key_path} -o StrictHostKeyChecking=no ubuntu@${aws_instance.twitter_filtered_stream_ec2_instance.public_dns}"
##  }
#}
#
#
