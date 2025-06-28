@echo off
echo ========================================
echo GENERATING BCRYPT HASH FOR admin123
echo ========================================
echo.

echo Compiling and running hash generator...
echo.

mvn compile exec:java -Dexec.mainClass="com.licentarazu.turismapp.util.SimpleBCryptHasher" -q

echo.
echo ========================================
echo.
pause
