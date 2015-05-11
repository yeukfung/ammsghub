rsync -avz -e "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" --progress target/universal/stage amweb01.do:/home/yf/app/ammsghub/
