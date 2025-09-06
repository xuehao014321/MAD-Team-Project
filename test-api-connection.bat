@echo off
echo Testing API connection...
echo.

echo Testing main network (192.168.0.119:5000):
curl -X GET "http://192.168.0.119:5000/api/test" --connect-timeout 5
echo.
echo.

echo Testing VMware network (192.168.56.1:5000):
curl -X GET "http://192.168.56.1:5000/api/test" --connect-timeout 5
echo.
echo.

echo Testing items endpoint (192.168.0.119:5000):
curl -X GET "http://192.168.0.119:5000/api/items" --connect-timeout 5
echo.
echo.

echo Testing items endpoint (192.168.56.1:5000):
curl -X GET "http://192.168.56.1:5000/api/items" --connect-timeout 5
echo.
echo.

pause
