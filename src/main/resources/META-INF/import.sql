insert into catalog(pk,id) values(1,'product_catalog');

insert into catalog_version(pk,id,catalog) values(10,'Staged',1);
insert into catalog_version(pk,id,catalog) values(11,'Online',1);

insert into product(pk,id,catalog_version) values(20,'my-product',10);

insert into currency(pk,id) values(30, 'usd');
insert into currency(pk,id) values(31, 'gbp');

insert into price(pk,id,value,product_pk,currency_pk) values(40,'my-product-usd-price',12.00,20,30);
insert into price(pk,id,value,product_pk,currency_pk) values(41,'my-product-gbp-price',10.00,20,31);