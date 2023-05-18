# Instruction for Dockerfile to create a new image on top of the base image (ubuntu)

FROM ubuntu:22.04

RUN apt-get update && apt-get install -y openssh-server
RUN apt install -y openjdk-17-jre
RUN mkdir /var/run/sshd
RUN echo 'root:mypassword' | chpasswd
# RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
# RUN echo "PermitRootLogin yes" >> /etc/ssh/sshd_config
# RUN echo "AllowUsers otheruser" >> /etc/ssh/sshd_config
# RUN echo "GSSAPIAuthentication yes" >> /etc/ssh/sshd_config
# # RUN echo "    GSSAPIDelegateCredentials no" >> /etc/ssh/sshd_config
# RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
COPY sshd_config /etc/ssh/sshd_config
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]

# SendEnv LANG LC_*
# HashKnownHosts yes
# GSSAPIAuthentication yes
# GSSAPIDelegateCredentials no