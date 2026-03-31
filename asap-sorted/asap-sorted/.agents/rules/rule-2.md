---
trigger: always_on
---

Optimization hi ek app ko MVP se actual profitable SaaS banata hai. Ek 4GB RAM wale device par hazaron images aur heavy ML models ek sath chalana phone ko freeze kar dega aur battery drain karega. Isko counter karne ke liye hume "Multi-Layer Filtration" aur "Memory-Safe Architecture" use karna hoga.

Yahan 4 core optimization strategies hain jo tumhari app ko low-spec devices par smooth chalayengi aur uniqueness dengi:

1. The "Metadata First" Rule (Bina AI ke 50% kaam)
mera app android kei file system ko pehle sei hi samajhta hoga (jaisa folder ka name vaisi uskee andar files) aur voh unn sab documents ko scanning list mei daal dega jo aactually kam ki hai aur unn sabko chod dega jo bloat hai (innn 100 % waste bloat files ko ek click mei delete karnei ka feature bhi show karna chaiyee mere app kei ui m. 

AI model ko har file ke liye call karna sabse badi galti hai. Pehle file ki Metadata (Data about data) check karo.

Path Analysis: Agar file /WhatsApp/Media/WhatsApp Documents/ mein hai aur extension .pdf ya .docx hai, toh uske important hone ke chances 90% hain.

Hash Matching: Jaisa pehle discuss kiya, pHash use karke duplicate aur known spam files ko filter out kar do. AI sirf unhi files par lagao jo inn basic filters ko bypass kar lein.

2. Quantized TFLite Models (Low-RAM AI)
Standard AI models (30-50MB) phone ki aadhi RAM kha jayenge. Tumhe TensorFlow Lite (TFLite) INT8 Quantized models use karne hain.

Yeh models size mein 5x chote hote hain (approx 3-5MB) aur RAM ka minimal use karte hain.

Accuracy mein bas 1-2% ka drop aata hai, lekin speed aur low-end hardware compatibility 10x badh jati hai. Google ML Kit natively kafi optimized hai, par custom models ke liye hamesha quantization use karo.

3. Chunk Processing via WorkManager
Badi galti: 4000 files ka array ek sath loop mein daal dena. OS turant app ko "Not Responding" bol kar kill kar dega.

Android WorkManager: Iska use karo. Scanning process ko 50-50 files ke "chunks" (tukdo) mein baant do.

App background mein 50 files scan karegi, RAM clear karegi (Garbage Collection), save karegi apnee app kei data kei folder mei,  aur phir next 50 scan karegi. Isey device kabhi heat nahi hoga.

4. UI Memory Optimization (Jetpack Compose + Coil)
Jab result screen par 1000 "Waste" images dikhani hon, toh sabko full resolution mein load mat karna.

UI ke liye LazyColumn (Jetpack Compose) use karo. Yeh sirf wahi images render karta hai jo screen par dikh rahi hain.

Image loading ke liye Coil library ka use karo aur usme aggressive downscaling set karo (e.g., load 1080p image as 150p thumbnail).

The Unique Selling Proposition (USP)
Ek "Eco-Scan Mode" ya "Smart Scan" feature do. Users ko batao ki yeh mode unke phone ki battery aur RAM bachata hai kyunki yeh pehle metadata aur hashes use karta hai, aur AI ka use sirf tab karta hai jab zaroorat ho. Yeh transparency users ko bahut pasand aati hai.

Kya hum pehle "Metadata aur File Hash" nikalne wala ek optimized Kotlin function likhne se start karein, jo app ka base engine banega?

dekho jaise gemini sirf 5 mb ka hai  jabki chatgpt 100 mb ka because Pre-installed OS (Operating System) Libraries: Gemini app Google ka apna product hai. Android phones mein Google App aur Google Play Services pehle se install hote hain. Gemini ka 6-7 MB (Megabyte) app sirf ek "Wrapper" hai jo phone ke in existing components ko trigger karta hai. Wahi dusri taraf, ChatGPT ek standalone (alag) app hai. Usko apne saare UI (User Interface) components, fonts, aur custom design libraries app ke andar khud pack karke deni padti hain.

toh i wnat ki mera app bhi yeh pre existing google services ka use karee aur sari ai processing offine karee fast karee, optimised kare.  

