#!/bin/bash

#
# this script is triggered by postfix each time an email is received to check the sender and recipient
# addresses in order to determine whether to add requests to a queue (a simple file listing based one)
# for another periodic script that will in turn generate (or revoke) openvpn client certificates
# and send back emails to the respective requesters
#

src_usr=''
dst_usr=''

queue_dir='/var/tmp/ovpn-cert-queue'

# make sure a pipe exists on stdin
[ -p /dev/stdin ] || exit 1

while IFS= read line; do
  # break on empty line (end of mail header section)
  [ -z "$line" ] && break
  # parse source and destination local-part and make sure it's coming from bfc.com
  if [[ "$line" == 'Return-Path: <'*'@bfc.com>' ]]; then
    src_usr=$(echo "${line}" | sed -e 's/Return-Path: <\(.*\)@bfc.com>/\1/')
  fi
  if [[ "$line" == 'X-Original-To: '*'@teleworking.bfc.com' ]]; then
    dst_usr=$(echo "${line}" | sed -e 's/X-Original-To: \(.*\)@teleworking.bfc.com/\1/')
  fi
done

# exit if something failed to parse
[[ -z "$src_usr" || -z "$dst_usr" ]] && exit 2

# make sure queue dirs exist
[ -d ${queue_dir} ] || mkdir ${queue_dir}
[ -d ${queue_dir}/generate ] || mkdir ${queue_dir}/generate
[ -d ${queue_dir}/regenerate ] || mkdir ${queue_dir}/regenerate
[ -d ${queue_dir}/revoke ] || mkdir ${queue_dir}/revoke

# which mailbox/request
case "$dst_usr" in
  'request.access')
    touch "${queue_dir}/generate/$src_usr"
    ;;
  'certificate.problem')
    touch "${queue_dir}/regenerate/$src_usr"
    ;;
  'definitive.departure')
    # HR-only request
    [[ "$src_usr" == 'hr' ]] || exit 3
    # concerned email address should be first line of body
    read line
    # check address validity
    if [ ! -z "$line" ] && [[ "$line" == *'@teleworking.bfc.com' ]] && [[ "${line%@teleworking.bfc.com}" =~ ^[a-zA-Z0-9._-]+$ ]]; then
      touch "${queue_dir}/revoke/${line%@teleworking.bfc.com}"
    fi
    ;;
esac