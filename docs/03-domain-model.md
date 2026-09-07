# Domain Model

**Café POS — Advanced Software Engineering Project**

> Written by Vanessa (issue #6). Add the domain model **diagram** (draw.io / a
> Mermaid `classDiagram`) above or below this explanation before submission.

## Explanation

The Café Point of Sale domain model represents the main real-world concepts involved in the operation of the café. It shows the important conceptual classes, their attributes and the relationships between them. As a domain model, it focuses on business concepts and does not include software methods, database data types, primary keys or foreign keys.

The **Staff** class represents employees who operate the system. Staff members have a username, full name, role and account status. One staff member can record several sales and process several refunds, while each sale and refund is handled by one staff member.

The **Customer** class stores the name, phone number and registration date of a customer. Attaching a customer to a sale is optional because the system also supports walk-in customers. A registered customer can therefore be associated with several sales, while an individual sale can have either one customer or no customer.

The **Category** class is used to group related menu items, such as coffee, tea, pastries and cold drinks. One category can contain several products, but each product belongs to one category.

The **Product** class represents an item offered for sale by the café. It contains the product name, price and availability status. A product can appear on several sale lines because it may be purchased in different transactions.

The **Sale** class represents a completed customer transaction. It records the date and time of the sale, subtotal, VAT amount, total amount and sale status. Each sale must contain one or more sale lines.

The **SaleLine** class represents a particular product included in a sale. It records the product name, unit price, quantity purchased and quantity already refunded. Keeping the product name and price at the time of the sale ensures that historical receipts remain accurate even when product information changes later.

The **Refund** class represents money returned to a customer for items from a previous sale. It records the date of the refund, reason and total refunded amount. A sale may have no refunds or several partial refunds.

The **RefundLine** class identifies the particular sale line and quantity included in a refund. Each refund must contain one or more refund lines. A sale line may also appear in multiple refund lines when items are returned through separate partial refunds.

Overall, the domain model provides a clear conceptual view of how staff, customers, products, sales and refunds interact within the Café POS system. It will guide the development of the database design and the software design class diagram.
