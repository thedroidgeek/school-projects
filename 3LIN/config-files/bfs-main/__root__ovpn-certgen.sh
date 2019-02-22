#!/bin/bash

#
# this script is being fired every 5mins to check the queue for openvpn certificate requests
# and act by (re)generating/revoking certs for clients that are either requesting access
# or that are being restricted from access by the HR, before sending back the result
# of the operations to the concerned users via their respective @bfc.com addresses
#

queue_dir='/var/tmp/ovpn-cert-queue'

generate_full_conf() {
  # used by ./build-key
  ./pkitool $1
  # generate the new 4-in-1 format config file if template exists
  if [ -f 'template.ovpn' ]; then
    (
      cat template.ovpn
      echo '<key>'
      cat keys/$1.key
      echo '</key>'
      echo '<cert>'
      cat keys/$1.crt
      echo '</cert>'
      echo '<ca>'
      cat keys/ca.crt
      echo '</ca>'
    ) > $1.ovpn
  fi
}

# make sure queue dirs exist
[ -d ${queue_dir} ] && [ -d ${queue_dir}/generate ] && [ -d ${queue_dir}/regenerate ] && [ -d ${queue_dir}/revoke ] || exit 1

# try mount the drbd device if not already mounted
if [ $(mount | grep -c /dev/drbd1) != 1 ]; then
  drbdadm primary --force storage
  mount /dev/drbd1 /mnt/pkistorage/ || exit 2
fi

# make sure we're also able to access the files
[ -d '/mnt/pkistorage/easy-rsa/' ] || exit 3

# set up env
cd /mnt/pkistorage/easy-rsa/
. vars

for user in "`ls ${queue_dir}/generate`"
do
  # break on empty folder
  [ -z "$user" ] && break
  # only generate if key and cert doesn't already exist
  if [ ! -f keys/"$user".key ] || [ ! -f keys/"$user".crt ]; then
    # invalidate old cert - just in case
    ./revoke-full "$user"
    cp keys/crl.pem /etc/openvpn/
    rm keys/"$user".*
    # generate client key and cert and make ovpn config file
    generate_full_conf "$user"
  fi
  # send mail back to user with generated files as attachments
  [ -f 'template.ovpn' ] && attachovpn="-a keys/$user.ovpn"
  echo "Check attached files." | mail -s "VPN access granted" -r noreply@teleworking.bfc.com keys/"$user".key keys/"$user".crt keys/ca.crt "$attachovpn" "$user"@bfc.com
  # delete generated config
  [ -f "$user".ovpn ] && rm "$user".ovpn
  rm "${queue_dir}/generate/$user"
done

for user in "`ls ${queue_dir}/regenerate`"
do
  [ -z "$user" ] && break
  # revoke previous cert if found
  if [ -f keys/"$user".key ] || [ -f keys/"$user".crt ]; then
    ./revoke-full "$user"
    cp keys/crl.pem /etc/openvpn/
    rm keys/"$user".*
  fi
  generate_full_conf "$user"
  [ -f 'template.ovpn' ] && attachovpn="-a keys/$user.ovpn"
  echo "Check attached files." | mail -s "VPN new certificate generated" -r noreply@teleworking.bfc.com -a keys/"$user".key keys/"$user".crt keys/ca.crt "$attachovpn" "$user"@bfc.com
  [ -f "$user".ovpn ] && rm "$user".ovpn
  rm "${queue_dir}/regenerate/$user"
done

for user in "`ls ${queue_dir}/revoke`"
do
  [ -z "$user" ] && break
  ./revoke-full "$user"
  cp keys/crl.pem /etc/openvpn/
  rm keys/"$user".*
  # confirm revocation to HR
  echo "Your request for user: '$user' has been processed." | mail -s "VPN access revocation request" -r noreply@teleworking.bfc.com hr@bfc.com
  rm "${queue_dir}/revoke/$user"
done