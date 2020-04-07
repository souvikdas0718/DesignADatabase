drop table if exists purchaseDetails;
drop table if exists orderPurchased;

#Table 1;
create table orderPurchased
(
    OrderID     int(11) auto_increment,
    SupplierID  int(11),
    placedDate  Date,
    arrivedDate Date,
    ShipperID   int(11),
    trackingID  varchar(20),
    constraint pk primary key (OrderID),
    constraint `fk_1` foreign key (`SupplierID`) references `suppliers` (`SupplierID`),
    constraint `fk_2` foreign key (`ShipperID`) references `shippers` (`ShipperID`)
);

#Table 2:
create table purchaseDetails
(
    OrderID     int(11),
    ProductID   int(11),
    Units int,
    productCost decimal(10, 4),
    constraint PK_1 PRIMARY KEY (OrderID, ProductID),
    constraint `FK_3` foreign key (`OrderID`) references `orderPurchased` (`OrderID`),
    constraint `FK_4` foreign key (`ProductID`) references `products` (`ProductID`)
);
