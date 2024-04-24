@echo off
echo DatabaseLayer
docker-compose -p database-layer -f DatabaseLayer/docker-compose.yml up -d

echo ApplicationLayer - NotificationApp
docker-compose -p application-layer -f ApplicationLayer/NotificationApp/docker-compose.yml up -d

echo ExternalLayer - ExternalNotificationLayer
docker-compose -p external-notification-layer -f ExternalLayer/ExternalNotificationLayer/CustomApp/docker-compose.yml up -d

echo Done
