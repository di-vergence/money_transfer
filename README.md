# Money transfer Rest API

A Java RESTful API for money transfers between users accounts

- http://localhost:8080/ 
- http://localhost:8080/accounts/ 
- http://localhost:8080/accounts/account/ 
- http://localhost:8080/accounts/:accountID/amount
- http://localhost:8080/accounts/:accountID/transfer

### Available Services

| HTTP METHOD | PATH | USAGE |
| -----------| ------ | ------ |
| GET | / | info about service | 
| GET | /accounts | get all accounts | 
| POST | /accounts/account | create new account | 
| GET | /accounts/:accountID/amount | get amount from account | 
| PUT | /accounts/:accountID/amount | add amount to account | 
| DELETE | /accounts/:accountID/transfer | transfer money between accounts | 
