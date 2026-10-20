package com.example.libraryreg

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

val WarnaUtama = Color(0xFF00796B)
val WarnaSukses = Color(0xFF558B2F)
val WarnaAksen = Color(0xFFE08A00)
val WarnaPlum = Color(0xFF8E3B6E)
val WarnaBahaya = Color(0xFFD84315)
val WarnaLatar = Color(0xFFF4F9F7)
val WarnaKartu = Color(0xFFE3F2EE)
val WarnaGaris = Color(0xFFB2CFC8)
val WarnaTeksUtama = Color(0xFF1B2B28)
val WarnaTeksRedup = Color(0xFF4F625E)

val BentukTombol = RoundedCornerShape(26.dp)
val BentukKolom = RoundedCornerShape(10.dp)
val BentukKartu = RoundedCornerShape(18.dp)

data class Anggota(
    val nama: String,
    val nomorInduk: String,
    val programStudi: String,
    val email: String,
    val telepon: String,
    val alamat: String,
    val jenis: String
)

enum class Layar { BERANDA, FORMULIR, DAFTAR, PENCARIAN, KELOLA, DETAIL, EDIT }

private val dataAnggota = mutableStateListOf<Anggota>()

fun simpanData(context: Context, daftar: List<Anggota>) {
    val array = JSONArray()
    daftar.forEach {
        array.put(
            JSONObject()
                .put("nama", it.nama)
                .put("nomorInduk", it.nomorInduk)
                .put("programStudi", it.programStudi)
                .put("email", it.email)
                .put("telepon", it.telepon)
                .put("alamat", it.alamat)
                .put("jenis", it.jenis)
        )
    }
    context.getSharedPreferences("libraryreg", Context.MODE_PRIVATE)
        .edit()
        .putString("anggota", array.toString())
        .apply()
}

fun muatData(context: Context): List<Anggota> {
    val teks = context.getSharedPreferences("libraryreg", Context.MODE_PRIVATE)
        .getString("anggota", "[]") ?: "[]"
    val array = JSONArray(teks)
    return List(array.length()) { i ->
        val o = array.getJSONObject(i)
        Anggota(
            o.getString("nama"),
            o.getString("nomorInduk"),
            o.getString("programStudi"),
            o.getString("email"),
            o.getString("telepon"),
            o.getString("alamat"),
            o.getString("jenis")
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (dataAnggota.isEmpty()) dataAnggota.addAll(muatData(this))
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = WarnaUtama,
                    onPrimary = Color.White,
                    background = WarnaLatar,
                    surface = WarnaLatar,
                    onSurface = WarnaTeksUtama,
                    error = WarnaBahaya
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = WarnaLatar) {
                    LibraryRegApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryRegApp() {
    val context = LocalContext.current
    var layar by rememberSaveable { mutableStateOf(Layar.BERANDA) }
    var asal by rememberSaveable { mutableStateOf(Layar.BERANDA) }
    var kembaliEdit by rememberSaveable { mutableStateOf(Layar.DAFTAR) }
    var nomorTerpilih by rememberSaveable { mutableStateOf("") }
    var hapusTarget by remember { mutableStateOf<Anggota?>(null) }

    fun kembali() {
        layar = when (layar) {
            Layar.FORMULIR, Layar.DETAIL -> asal
            Layar.EDIT -> kembaliEdit
            else -> Layar.BERANDA
        }
    }

    fun lihat(anggota: Anggota, dari: Layar) {
        nomorTerpilih = anggota.nomorInduk
        asal = dari
        layar = Layar.DETAIL
    }

    fun edit(anggota: Anggota, kembaliKe: Layar) {
        nomorTerpilih = anggota.nomorInduk
        kembaliEdit = kembaliKe
        layar = Layar.EDIT
    }

    BackHandler(enabled = layar != Layar.BERANDA) { kembali() }

    val judul = when (layar) {
        Layar.BERANDA -> "LibraryReg"
        Layar.FORMULIR -> "Formulir Pendaftaran"
        Layar.DAFTAR -> "Daftar Anggota"
        Layar.PENCARIAN -> "Pencarian Data"
        Layar.KELOLA -> "Kelola Data"
        Layar.DETAIL -> "Detail Anggota"
        Layar.EDIT -> "Edit Anggota"
    }

    Scaffold(
        containerColor = WarnaLatar,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        judul,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    if (layar != Layar.BERANDA) {
                        TextButton(onClick = { kembali() }) {
                            Text("‹", color = Color.White, fontSize = 30.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarnaUtama,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (layar) {
                Layar.BERANDA -> LayarBeranda(
                    onMenu = {
                        asal = Layar.BERANDA
                        layar = it
                    }
                )

                Layar.FORMULIR -> FormAnggota(
                    awal = null,
                    daftar = dataAnggota,
                    labelTombol = "Daftar",
                    onBatal = null,
                    onSimpan = {
                        dataAnggota.add(it)
                        simpanData(context, dataAnggota)
                        Toast.makeText(context, "Anggota berhasil didaftarkan", Toast.LENGTH_SHORT).show()
                        layar = Layar.DAFTAR
                    }
                )

                Layar.DAFTAR -> LayarDaftar(
                    daftar = dataAnggota,
                    onTambah = {
                        asal = Layar.DAFTAR
                        layar = Layar.FORMULIR
                    },
                    onLihat = { lihat(it, Layar.DAFTAR) },
                    onEdit = { edit(it, Layar.DAFTAR) },
                    onHapus = { hapusTarget = it }
                )

                Layar.PENCARIAN -> LayarPencarian(
                    daftar = dataAnggota,
                    onPilih = { lihat(it, Layar.PENCARIAN) }
                )

                Layar.KELOLA -> LayarKelola(
                    daftar = dataAnggota,
                    onEdit = { edit(it, Layar.KELOLA) },
                    onHapus = { hapusTarget = it }
                )

                Layar.DETAIL -> {
                    val anggota = dataAnggota.find { it.nomorInduk == nomorTerpilih }
                    if (anggota != null) {
                        LayarDetail(
                            anggota = anggota,
                            onEdit = { edit(anggota, Layar.DETAIL) },
                            onHapus = { hapusTarget = anggota }
                        )
                    }
                }

                Layar.EDIT -> {
                    val anggota = dataAnggota.find { it.nomorInduk == nomorTerpilih }
                    if (anggota != null) {
                        FormAnggota(
                            awal = anggota,
                            daftar = dataAnggota,
                            labelTombol = "Simpan",
                            onBatal = { layar = kembaliEdit },
                            onSimpan = { baru ->
                                val index = dataAnggota.indexOfFirst { it.nomorInduk == anggota.nomorInduk }
                                if (index >= 0) dataAnggota[index] = baru
                                simpanData(context, dataAnggota)
                                nomorTerpilih = baru.nomorInduk
                                Toast.makeText(context, "Perubahan disimpan", Toast.LENGTH_SHORT).show()
                                layar = kembaliEdit
                            }
                        )
                    }
                }
            }
        }
    }

    hapusTarget?.let { target ->
        DialogHapus(
            nama = target.nama,
            onBatal = { hapusTarget = null },
            onHapus = {
                dataAnggota.remove(target)
                simpanData(context, dataAnggota)
                hapusTarget = null
                Toast.makeText(context, "Data anggota dihapus", Toast.LENGTH_SHORT).show()
                if (layar == Layar.DETAIL) layar = asal
            }
        )
    }
}

@Composable
fun TombolMenu(teks: String, keterangan: String, warna: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(76.dp),
        shape = BentukTombol,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = warna)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(teks, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
            Text(keterangan, fontSize = 12.sp, color = Color(0xFFEFFAF7))
        }
    }
}

@Composable
fun LayarBeranda(onMenu: (Layar) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "LibraryReg",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp,
            color = WarnaUtama
        )
        Text("Pendaftaran Anggota Perpustakaan", fontSize = 15.sp, color = WarnaTeksRedup)
        Spacer(modifier = Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TombolMenu("Pendaftaran Anggota", "Isi formulir anggota baru", WarnaUtama) { onMenu(Layar.FORMULIR) }
            TombolMenu("Daftar Anggota", "Lihat semua anggota terdaftar", WarnaSukses) { onMenu(Layar.DAFTAR) }
            TombolMenu("Pencarian Data", "Cari berdasarkan nama atau nomor induk", WarnaAksen) { onMenu(Layar.PENCARIAN) }
            TombolMenu("Kelola Data", "Ubah dan hapus data anggota", WarnaPlum) { onMenu(Layar.KELOLA) }
        }
    }
}

fun validasi(
    nama: String,
    nomor: String,
    prodi: String,
    email: String,
    telepon: String,
    alamat: String,
    daftar: List<Anggota>,
    nomorLama: String?
): Map<String, String> {
    val error = mutableMapOf<String, String>()

    if (nama.isBlank()) error["nama"] = "Nama wajib diisi"

    if (nomor.isBlank()) {
        error["nomor"] = "NIM/NIP wajib diisi"
    } else if (!nomor.all { it.isDigit() } || nomor.length < 8 || nomor.length > 18) {
        error["nomor"] = "NIM/NIP harus berupa angka (8 sampai 18 digit)"
    } else if (daftar.any { it.nomorInduk == nomor && it.nomorInduk != nomorLama }) {
        error["nomor"] = "NIM/NIP sudah terdaftar"
    }

    if (prodi.isBlank()) error["prodi"] = "Program studi / unit wajib diisi"

    if (email.isBlank()) {
        error["email"] = "Email wajib diisi"
    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        error["email"] = "Format email tidak valid"
    }

    if (telepon.isBlank()) {
        error["telepon"] = "Nomor telepon wajib diisi"
    } else if (!telepon.all { it.isDigit() } || telepon.length < 9 || telepon.length > 15) {
        error["telepon"] = "Nomor telepon harus angka, 9 sampai 15 digit"
    }

    if (alamat.isBlank()) error["alamat"] = "Alamat wajib diisi"

    return error
}

@Composable
fun warnaKolom() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WarnaUtama,
    unfocusedBorderColor = WarnaGaris,
    focusedLabelColor = WarnaUtama,
    cursorColor = WarnaUtama,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

@Composable
fun KolomIsian(
    label: String,
    nilai: String,
    onChange: (String) -> Unit,
    error: String?,
    keyboard: KeyboardType = KeyboardType.Text,
    banyakBaris: Int = 1
) {
    OutlinedTextField(
        value = nilai,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        singleLine = banyakBaris == 1,
        minLines = banyakBaris,
        shape = BentukKolom,
        colors = warnaKolom(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FormAnggota(
    awal: Anggota?,
    daftar: List<Anggota>,
    labelTombol: String,
    onBatal: (() -> Unit)?,
    onSimpan: (Anggota) -> Unit
) {
    var nama by rememberSaveable { mutableStateOf(awal?.nama ?: "") }
    var nomor by rememberSaveable { mutableStateOf(awal?.nomorInduk ?: "") }
    var prodi by rememberSaveable { mutableStateOf(awal?.programStudi ?: "") }
    var email by rememberSaveable { mutableStateOf(awal?.email ?: "") }
    var telepon by rememberSaveable { mutableStateOf(awal?.telepon ?: "") }
    var alamat by rememberSaveable { mutableStateOf(awal?.alamat ?: "") }
    var jenis by rememberSaveable { mutableStateOf(awal?.jenis ?: "Mahasiswa") }
    var menuTerbuka by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(mapOf<String, String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KolomIsian("Nama Lengkap", nama, { nama = it }, error["nama"])
        KolomIsian("NIM/NIP", nomor, { nomor = it }, error["nomor"], KeyboardType.Number)
        KolomIsian("Program Studi / Unit", prodi, { prodi = it }, error["prodi"])
        KolomIsian("Email", email, { email = it }, error["email"], KeyboardType.Email)
        KolomIsian("Nomor Telepon", telepon, { telepon = it }, error["telepon"], KeyboardType.Phone)
        KolomIsian("Alamat", alamat, { alamat = it }, error["alamat"], banyakBaris = 3)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { menuTerbuka = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = BentukKolom,
                border = BorderStroke(1.5.dp, WarnaUtama),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = WarnaUtama
                )
            ) { Text("Jenis Keanggotaan: $jenis") }
            DropdownMenu(expanded = menuTerbuka, onDismissRequest = { menuTerbuka = false }) {
                listOf("Mahasiswa", "Dosen").forEach { pilihan ->
                    DropdownMenuItem(
                        text = { Text(pilihan) },
                        onClick = {
                            jenis = pilihan
                            menuTerbuka = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val hasil = validasi(
                    nama.trim(), nomor.trim(), prodi.trim(), email.trim(),
                    telepon.trim(), alamat.trim(), daftar, awal?.nomorInduk
                )
                error = hasil
                if (hasil.isEmpty()) {
                    onSimpan(
                        Anggota(
                            nama.trim(), nomor.trim(), prodi.trim(), email.trim(),
                            telepon.trim(), alamat.trim(), jenis
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = BentukTombol,
            colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)
        ) { Text(labelTombol, fontSize = 16.sp, fontWeight = FontWeight.Medium) }

        if (onBatal != null) {
            OutlinedButton(
                onClick = onBatal,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = BentukTombol,
                border = BorderStroke(1.5.dp, WarnaTeksRedup),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarnaTeksRedup)
            ) { Text("Batal", fontSize = 16.sp) }
        }
    }
}

@Composable
fun Garis() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(WarnaGaris))
}

@Composable
fun IkonAksi(simbol: String, warna: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(warna, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(simbol, fontSize = 13.sp, color = Color.White) }
}

@Composable
fun BarisTabel(
    no: String,
    nama: String,
    nomor: String,
    jenis: String,
    tebal: Boolean,
    aksi: @Composable () -> Unit
) {
    val gaya = if (tebal) FontWeight.Bold else FontWeight.Normal
    val latar = if (tebal) Modifier.background(WarnaKartu, RoundedCornerShape(8.dp)) else Modifier
    Row(
        modifier = Modifier.fillMaxWidth().then(latar).padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(no, modifier = Modifier.weight(0.5f), fontSize = 12.sp, fontWeight = gaya, color = WarnaTeksUtama)
        Text(nama, modifier = Modifier.weight(1.6f), fontSize = 12.sp, fontWeight = gaya, color = WarnaTeksUtama)
        Text(nomor, modifier = Modifier.weight(1.5f), fontSize = 12.sp, fontWeight = gaya, color = WarnaTeksUtama)
        Text(jenis, modifier = Modifier.weight(1.3f), fontSize = 12.sp, fontWeight = gaya, color = WarnaTeksUtama)
        Box(modifier = Modifier.weight(1.9f)) { aksi() }
    }
}

@Composable
fun LayarDaftar(
    daftar: List<Anggota>,
    onTambah: () -> Unit,
    onLihat: (Anggota) -> Unit,
    onEdit: (Anggota) -> Unit,
    onHapus: (Anggota) -> Unit
) {
    var cari by rememberSaveable { mutableStateOf("") }
    val hasil = daftar.filter {
        it.nama.contains(cari, ignoreCase = true) || it.nomorInduk.contains(cari, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = cari,
            onValueChange = { cari = it },
            label = { Text("Cari nama atau nomor induk...") },
            singleLine = true,
            shape = BentukKolom,
            colors = warnaKolom(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onTambah,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = BentukTombol,
            colors = ButtonDefaults.buttonColors(containerColor = WarnaUtama)
        ) { Text("+ Tambah", fontWeight = FontWeight.Medium) }

        BarisTabel("No", "Nama", "Nomor Induk", "Jenis", true) {
            Text("Aksi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarnaTeksUtama)
        }

        if (hasil.isEmpty()) {
            Text(
                text = if (daftar.isEmpty()) "Belum ada anggota" else "Anggota tidak ditemukan",
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                fontSize = 15.sp,
                color = WarnaTeksRedup
            )
        } else {
            LazyColumn {
                itemsIndexed(hasil, key = { _, a -> a.nomorInduk }) { index, a ->
                    BarisTabel((index + 1).toString(), a.nama, a.nomorInduk, a.jenis, false) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IkonAksi("◉", WarnaUtama) { onLihat(a) }
                            IkonAksi("✎", WarnaAksen) { onEdit(a) }
                            IkonAksi("✖", WarnaBahaya) { onHapus(a) }
                        }
                    }
                    Garis()
                }
            }
        }
    }
}

@Composable
fun LayarPencarian(daftar: List<Anggota>, onPilih: (Anggota) -> Unit) {
    var cari by rememberSaveable { mutableStateOf("") }
    val hasil = if (cari.isBlank()) emptyList() else daftar.filter {
        it.nama.contains(cari, ignoreCase = true) || it.nomorInduk.contains(cari, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = cari,
            onValueChange = { cari = it },
            label = { Text("Nama atau nomor induk") },
            singleLine = true,
            shape = BentukKolom,
            colors = warnaKolom(),
            modifier = Modifier.fillMaxWidth()
        )

        if (cari.isBlank()) {
            Text("Ketik nama atau nomor induk untuk mencari anggota.", fontSize = 14.sp, color = WarnaTeksRedup)
        } else {
            Text(
                "Ditemukan ${hasil.size} anggota",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarnaUtama
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(hasil, key = { _, a -> a.nomorInduk }) { _, a ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onPilih(a) },
                        shape = BentukKartu,
                        border = BorderStroke(1.dp, WarnaGaris),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = WarnaKartu)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                a.nama,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = WarnaTeksUtama
                            )
                            Text(a.nomorInduk, color = WarnaTeksRedup)
                            Text(a.jenis, color = WarnaUtama, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LayarKelola(daftar: List<Anggota>, onEdit: (Anggota) -> Unit, onHapus: (Anggota) -> Unit) {
    if (daftar.isEmpty()) {
        Text("Belum ada anggota", modifier = Modifier.padding(20.dp), fontSize = 15.sp, color = WarnaTeksRedup)
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(daftar, key = { _, a -> a.nomorInduk }) { _, a ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = BentukKartu,
                border = BorderStroke(1.dp, WarnaGaris),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = WarnaKartu)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column {
                        Text(
                            a.nama,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = WarnaTeksUtama
                        )
                        Text("${a.nomorInduk} • ${a.jenis}", color = WarnaTeksRedup)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onEdit(a) },
                            modifier = Modifier.weight(1f),
                            shape = BentukTombol,
                            colors = ButtonDefaults.buttonColors(containerColor = WarnaAksen)
                        ) { Text("Edit") }
                        Button(
                            onClick = { onHapus(a) },
                            modifier = Modifier.weight(1f),
                            shape = BentukTombol,
                            colors = ButtonDefaults.buttonColors(containerColor = WarnaBahaya)
                        ) { Text("Hapus") }
                    }
                }
            }
        }
    }
}

@Composable
fun BarisDetail(label: String, nilai: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = WarnaUtama, fontWeight = FontWeight.Medium)
        Text(nilai, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = WarnaTeksUtama)
    }
}

@Composable
fun LayarDetail(anggota: Anggota, onEdit: () -> Unit, onHapus: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BarisDetail("Nama Lengkap", anggota.nama)
        BarisDetail("NIM/NIP", anggota.nomorInduk)
        BarisDetail("Program Studi / Unit", anggota.programStudi)
        BarisDetail("Email", anggota.email)
        BarisDetail("Nomor Telepon", anggota.telepon)
        BarisDetail("Alamat", anggota.alamat)
        BarisDetail("Jenis Keanggotaan", anggota.jenis)

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = BentukTombol,
                colors = ButtonDefaults.buttonColors(containerColor = WarnaAksen)
            ) { Text("Edit") }
            Button(
                onClick = onHapus,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = BentukTombol,
                colors = ButtonDefaults.buttonColors(containerColor = WarnaBahaya)
            ) { Text("Hapus") }
        }
    }
}

@Composable
fun DialogHapus(nama: String, onBatal: () -> Unit, onHapus: () -> Unit) {
    AlertDialog(
        onDismissRequest = onBatal,
        shape = BentukKartu,
        containerColor = Color.White,
        title = { Text("Hapus data?", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = { Text("Yakin ingin menghapus data $nama?") },
        confirmButton = {
            TextButton(onClick = onHapus) { Text("Hapus", color = WarnaBahaya, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onBatal) { Text("Batal", color = WarnaTeksRedup) }
        }
    )
}