#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backups"
DB_NAME="hospital_queue"
DB_USER="root"
DB_PASSWORD="root@123"

mkdir -p $BACKUP_DIR

echo "Starting backup at $DATE"
mysqldump -u$DB_USER -p$DB_PASSWORD $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

if [ $? -eq 0 ]; then
    echo "Backup successful"
    find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
    echo "Old backups cleaned"
else
    echo "Backup FAILED"
    exit 1
fi