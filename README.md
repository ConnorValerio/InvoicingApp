# InvoicingApp
JavaFX Invoicing Application

Project Description
-------------------
A JavaFX GUI Application to generate & send PDF invoices via email. Functionality includes: 

- registering a company
- creating/viewing/deleting contacts and products
- generating pdf invoices
- sending pdf invoices via registerd gmail account
- search for previously created invoices
- view pdf invoices via your default web browser 

HOW TO:
--------

- Register a company, choose logo, add gmail account / password etc.
- Add a contact on the top RHS panel
- Add a product on the bottom RHS panel
- Have both the contact, and the product(s) you want to create an invoice for, selected.
- Click 'Create Invoice'
- Choose whether to just generate the pdf or whether to send it too.

Invoices can be viewed by double clicking them in the tree structure, or by searching in the top LHS panel.

NOTE:
-----

- This application only works if the invoice is being sent from a gmail account, didn't add functionality for other providers.
- Application state is restored from an xml file (state.xml) and the Invoices folder. Any edit to these folders/files may cause unpredicted behaviour.
- RegEx used in validating was not thoroughly checked.
