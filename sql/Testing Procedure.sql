-- Testing procedure

-- mengambil data karyawan (menu admin)
go
create proc get_data_karyawan
as
select k.id_karyawan, k.nama, k.jenis_kelamin, k.tanggal_lahir, k.nomor_hp, k.email, k.alamat, k.jabatan,
    c.nama_cabang, j.gaji
from karyawan k
left outer join cabang c on k.id_cabang=c.id_cabang
left outer join jabatan j on j.nama_jabatan=k.jabatan
go

-- exec get_data_karyawan

-- mengambil data pelanggan (menu admin)
go
create proc get_data_pelanggan
as
select cp.id_pelanggan, cp.nama_pelanggan, cp.tanggal_lahir, cp.nomor_hp, cp.jenis_kelamin, ca.email,
    cp.jalan + ' no ' + cp.nomor_rumah + ' desa/kecamatan ' + cp.desa_kecamatan + ' kabupaten/kota ' + cp.kabupaten_kota + ' kode pos ' + cp.kode_pos as alamat,
    (select count(id_pelanggan) from order_product op where op.id_pelanggan=cp.id_pelanggan) as jumlah_order
from customer_profile cp
join customer_account ca on cp.id_pelanggan=ca.id_pelanggan
go

-- exec get_data_pelanggan

-- mengambil data supplier
go
create proc get_data_supplier
as
select s.id_supplier, s.nama_supplier, s.alamat, s.nomor_hp, s.email, 
    (select count(*) from supplied_product sp where sp.id_supplier=s.id_supplier) as jumlah_produk
from supplier s
go

-- exec get_data_supplier

-- Mengambil data supplier specific : digunakan untuk mengisi data pada form edit supplier
go
create procedure get_specific_supplier @id int
as
select nama_supplier, alamat, nomor_hp, email, kode_pos from supplier
where id_supplier = @id

-- exec get_specific_supplier 1

-- mengambil supplied product dari supplier tertentu
go
create procedure get_supplied_product @id_supplier int
as
select sp.id, sp.id_supplier, p.nama_produk, sp.jumlah_produk, sp.tanggal
from supplied_product sp
join produk p on p.product_id=sp.product_id
where sp.id_supplier = @id_supplier
go

-- exec get_supplied_product 2

-- mengambil data cabang (menu admin)
go

create procedure get_data_cabang
as
select id_cabang, nama_cabang, alamat_cabang, (select count(*) from karyawan where karyawan.id_cabang=cabang.id_cabang) as jumlah_karyawan
from cabang
go
-- exec get_data_cabang


-- mengambil data produk (tabel menu admin)
go
create procedure get_data_tabel_produk
as
select product_id, nama_produk, jumlah_stok, harga_satuan, kategori from produk
go
-- exec get_data_tabel_produk

-- mengambil data karyawan pada suatu cabang
go
create procedure get_karyawan_at_cabang @idCabang int
as
select k.nama, k.jabatan, j.gaji from karyawan k
join jabatan j on k.jabatan=j.nama_jabatan
where k.id_cabang = @idCabang
go
-- exec get_karyawan_at_cabang 1

-- detail produk
go
create procedure get_detail_produk @idProduk int
as
select nama_produk, jumlah_stok, harga_satuan, deskripsi, nutrition_facts, kategori from produk
where produk.product_id = @idProduk
go
-- exec get_detail_produk 1

-- update detail product
go
create procedure update_detail_product 
    @id int,
    @nama varchar(255),
    @jumlah int,
    @harga int,
    @desc text,
    @nutrition text,
    @kat varchar(100)
as
update produk
set nama_produk = @nama, 
    jumlah_stok = @jumlah, 
    harga_satuan = @harga, 
    deskripsi = @desc,
    nutrition_facts = @nutrition, 
    kategori = @kat
where product_id = @id

go

-- tambah produk baru
go
create proc new_product 
    @nama varchar(255),
    @jumlah int,
    @harga int,
    @desc text,
    @nutrition text,
    @kat varchar(100)
as
insert into produk (nama_produk, jumlah_stok, harga_satuan, deskripsi, nutrition_facts, kategori) 
values (@nama, @jumlah, @harga, @desc, @nutrition, @kat);
go

-- delete product
go
create procedure delete_product @id int
as
delete from produk where product_id = @id
go

-- cabang baru
go
create procedure new_cabang
    @nama varchar(100),
    @alamat text
as
insert into cabang values (@nama, @alamat)
go

-- delete cabang
go
create procedure delete_cabang @id int
as
delete from cabang where id_cabang = @id

-- tambah supplier
go
create procedure new_supplier
    @nama varchar(255),
    @alamat text,
    @nomor varchar(20),
    @email varchar(50),
    @pos varchar(5)
as
insert into supplier (nama_supplier, alamat, nomor_hp, email, kode_pos) 
values (@nama, @alamat, @nomor, @email, @pos);

-- exec new_supplier 'dimas', 'dimas', 'dimas', 'dimas@gmail', '12345'

-- update data supplier
go
create procedure update_supplier
    @id int,
    @nama varchar(255),
    @alamat text,
    @nomor varchar(20),
    @email varchar(50),
    @pos varchar(5)
as
update supplier
set nama_supplier = @nama,
    alamat = @alamat,
    nomor_hp = @nomor, 
    email = @email,
    kode_pos = @pos
where id_supplier = @id

-- exec update_supplier 6, 'testing', 'testing', '932438', 'testing@gemail', '12345'

-- menghapus supplier
go
create procedure delete_supplier @id int
as
delete from supplier where id_supplier = @id

-- exec delete_supplier 10


-- menambah supplied_product baru : TambahDataSupply : otomatis mengupdate jumlah stok produk di bagian produk
go
create procedure add_supplied_product
    @idProduk int,
    @idSupplier int,
    @jumlah int
as
begin tran
begin try
    insert into supplied_product (product_id, id_supplier, jumlah_produk, tanggal)
    values (@idProduk, @idSupplier, @jumlah, getdate());

    update produk
    set jumlah_stok = (jumlah_stok + @jumlah)
    where product_id = @idProduk

    if @@trancount > 0
        begin commit tran end
end try
begin catch
    if @@trancount > 0
        begin rollback tran end
end catch

-- menghapus supplied_product
go
create procedure delete_supplied_product
    @id int
as
begin tran
begin try
    update produk
    set jumlah_stok = (select iif((jumlah_stok - jumlah_produk)>0, jumlah_stok - jumlah_produk, 0) 
        from supplied_product where id = @id)
    where product_id = (select sp.product_id from supplied_product sp where id = @id)

    delete from supplied_product where id = @id;

    if @@trancount > 0
        begin commit tran end
end try
begin catch
    if @@trancount > 0
        begin rollback tran end
end catch

-- exec delete_supplied_product 4

-- tambah karyawan
go
create procedure add_karyawan
    @nama varchar(255),
    @jenisKelamin char(1),
    @tanggalLahir date,
    @nomor varchar(25),
    @email varchar(50),
    @alamat text,
    @cabang int,
    @jabatan varchar(50)
as
insert into karyawan (nama, jenis_kelamin, tanggal_lahir, nomor_hp, email, alamat, id_cabang, jabatan) 
values (@nama, @jenisKelamin, @tanggalLahir, @nomor, @email, @alamat, @cabang, @jabatan);

-- exec add_karyawan 'dimastri', 'L', '2000-12-12', '089656565', 'test@gmail.com', 'Malang', 1, 'Manager' 

--tambah akun pelanggan
go
create procedure add_customer_account
	@email varchar(50),
	@password varchar(255)
as
insert into customer_account (email, password)
values (@email, @password);

go
create proc add_customer_profile
	@id_pelanggan int,
	@nama_pelanggan varchar(255),
	@tanggal_lahir date,
	@nomor_hp varchar(25),
	@nomor_rumah varchar(5),
	@desa_kecamatan varchar(50),
	@kabupaten_kota varchar(50),
	@jalan varchar(50),
	@jenis_kelamin char(1),
	@kode_pos varchar(5)
as
insert into customer_profile (id_pelanggan, nama_pelanggan, tanggal_lahir, nomor_hp, nomor_rumah, desa_kecamatan, kabupaten_kota, jalan, jenis_kelamin, kode_pos)
values (@id_pelanggan, @nama_pelanggan,@tanggal_lahir ,@nomor_hp ,@nomor_rumah , @desa_kecamatan,@kabupaten_kota , @jalan,@jenis_kelamin ,@kode_pos);



--get riwayat pesanan
drop procedure get_riwayatPesanan
go
create proc get_riwayatPesanan
	@id_pelanggan int
as
select order_id, tanggal_kirim, status_order
from order_product where id_pelanggan = @id_pelanggan;

--get detail pesanan
drop proc get_detailpesanan
go
create proc get_detailpesanan
	@order_id int
as
select op.order_id,p.product_id, p.nama_produk, p.kategori, p.harga_satuan, op.kuantitas, (p.harga_satuan * op.kuantitas)as subtotal
from ordered_product op
join produk p on p.product_id = op.product_id
where op.order_id = @order_id;


--get pesanan aktif
drop proc get_pesananaktif
go
create proc get_pesananaktif
	@id_pelanggan int
as
select tanggal_kirim, order_id
from order_product
where id_pelanggan = @id_pelanggan and status_order = 0;
exec get_pesananaktif 3


--varel
go
create procedure get_profil_pelanggan @email varchar(100), @password varchar(40) as
select * from customer_account ca join customer_profile cp on ca.id_pelanggan = cp.id_pelanggan
where email=@email and password=@password
go

create procedure update_akun_pelanggan @id int, @email varchar(100), @password varchar(40) as
update customer_account set email=@email, password=@password
where id_pelanggan=@id
go

create procedure update_profil_pelanggan @id int, @nama varchar(20), @tgl date, @nomorHp varchar(14), 
@nomorRumah varchar (5), @desaKec varchar(30), @kabKota varchar(30), @jalan varchar(50), @jk varchar(1), 
@kodePos varchar(10) as
update customer_profile set nama_pelanggan=@nama, tanggal_lahir=@tgl,
nomor_hp=@nomorHp, nomor_rumah=@nomorRumah ,desa_kecamatan=@desaKec,
kabupaten_kota=@kabKota, jalan=@jalan, jenis_kelamin=@jk, kode_pos=@kodePos
where id_pelanggan=@id
go


-- tambah customer
drop proc add_customer
go
create procedure add_customer
    @email varchar(50), @password varchar(255),
    @nama varchar(20), @tgl date, @nomorHp varchar(14), 
    @nomorRumah varchar (5), @desaKec varchar(30), @kabKota varchar(30), 
    @jalan varchar(50), @jk varchar(1), @kodePos varchar(10)
as
begin tran
begin try
    insert into customer_account 
    values (@email, @password);

    declare @id as int = (select max(id_pelanggan) from customer_account)

    insert into customer_profile (id_pelanggan, nama_pelanggan, tanggal_lahir, nomor_hp, nomor_rumah, desa_kecamatan, kabupaten_kota, jalan, jenis_kelamin, kode_pos) 
    values (@id, @nama, @tgl, @nomorHp, @nomorRumah, @desaKec, @kabKota, @jalan, @jk, @kodePos);

    if @@trancount > 0
        begin commit tran end
end try
begin catch
    if @@trancount > 0
        begin rollback tran end
end catch
