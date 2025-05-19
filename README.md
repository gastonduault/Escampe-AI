# Escampe-AI


## Get started
First shell
```bash
java -cp escampeobf.jar escampe.ServeurJeu 1234 1
```
Second shell
```bash
javac escampe/*.java 
java -cp .:escampeobf.jar escampe.ClientJeu escampe.IA localhost 1234
```
Third Shell
```bash
java -cp escampeobf.jar escampe.ClientJeu escampe.JoueurAleatoire localhost 1234
```
