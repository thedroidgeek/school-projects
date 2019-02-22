resource storage {
        device /dev/drbd1;
        disk /dev/sdb1;
        meta-disk internal;
        on bfs-main {
                address 192.168.152.200:7788;
        }
        on bfs-backup {
                address 192.168.152.201:7788;
        }
}
