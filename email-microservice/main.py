from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr
from typing import Optional, List
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os
from dotenv import load_dotenv

# .env dosyasını yükle
load_dotenv()

app = FastAPI(
    title="Email Microservice",
    description="Spring Boot için Python Email Mikroservisi",
    version="1.0.0"
)

# CORS ayarları - Spring Boot'tan gelen isteklere izin ver
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Gmail SMTP ayarları (.env dosyasından)
MAIL_HOST = os.getenv("MAIL_HOST", "smtp.gmail.com")
MAIL_PORT = int(os.getenv("MAIL_PORT", 587))
MAIL_USER = os.getenv("MAIL_USER")
MAIL_PASS = os.getenv("MAIL_PASS")

# ============ DTO (Data Transfer Objects) ============

class EmailRequest(BaseModel):
    to: EmailStr
    subject: str
    body: str
    is_html: bool = True

class WelcomeEmailRequest(BaseModel):
    to: EmailStr
    kullanici_adi: str
    ad: str

class SiparisKalemi(BaseModel):
    urun_ad: str
    beden: str
    adet: int
    birim_fiyat: float
    toplam_fiyat: float

class SiparisOzetiRequest(BaseModel):
    to: EmailStr
    siparis_id: int
    musteri_ad: str
    magaza_ad: str
    toplam_tutar: float
    teslimat_adresi: str
    siparis_tarihi: str
    kalemler: List[SiparisKalemi]

class EmailResponse(BaseModel):
    success: bool
    message: str

# ============ EMAIL GÖNDERIM FONKSİYONU ============

def send_email(to: str, subject: str, html_body: str) -> bool:
    """Gmail SMTP ile email gönder"""
    try:
        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = MAIL_USER
        msg["To"] = to
        
        html_part = MIMEText(html_body, "html", "utf-8")
        msg.attach(html_part)
        
        with smtplib.SMTP(MAIL_HOST, MAIL_PORT) as server:
            server.starttls()
            server.login(MAIL_USER, MAIL_PASS)
            server.sendmail(MAIL_USER, to, msg.as_string())
        
        print(f"✅ Email gönderildi: {to}")
        return True
    except Exception as e:
        print(f"❌ Email gönderilemedi: {e}")
        return False

# ============ HTML ŞABLONLARI ============

def get_welcome_html(kullanici_adi: str, ad: str) -> str:
    """Hoşgeldin email şablonu"""
    return f"""
    <!DOCTYPE html>
    <html>
    <head><meta charset="UTF-8"></head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
        <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 28px;">🎉 Hoş Geldiniz!</h1>
            </div>
            <div style="padding: 30px;">
                <p style="font-size: 18px; color: #333;">
                    Merhaba <strong>{ad}</strong>,
                </p>
                <p style="color: #666; line-height: 1.6;">
                    Mağaza Sistemimize kayıt olduğunuz için teşekkür ederiz!
                </p>
                <div style="background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0;">
                    <h3 style="margin-top: 0; color: #667eea;">📋 Hesap Bilgileriniz</h3>
                    <p style="margin: 5px 0;"><strong>Kullanıcı Adı:</strong> {kullanici_adi}</p>
                </div>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="http://localhost:8080/" 
                       style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                              color: white; padding: 15px 40px; text-decoration: none; 
                              border-radius: 25px; font-weight: bold; display: inline-block;">
                        🛒 Alışverişe Başla
                    </a>
                </div>
            </div>
            <div style="background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                <p style="color: #999; font-size: 12px; margin: 0;">
                    © 2026 Mağaza Sistemi | Python Microservice ile gönderildi
                </p>
            </div>
        </div>
    </body>
    </html>
    """

def get_order_html(siparis: SiparisOzetiRequest) -> str:
    """Sipariş özeti email şablonu"""
    kalemler_html = ""
    for kalem in siparis.kalemler:
        kalemler_html += f"""
        <tr>
            <td style="padding: 12px; border-bottom: 1px solid #eee;">{kalem.urun_ad}</td>
            <td style="padding: 12px; text-align: center; border-bottom: 1px solid #eee;">{kalem.beden}</td>
            <td style="padding: 12px; text-align: center; border-bottom: 1px solid #eee;">{kalem.adet}</td>
            <td style="padding: 12px; text-align: right; border-bottom: 1px solid #eee;">{kalem.toplam_fiyat:.2f} TL</td>
        </tr>
        """
    
    return f"""
    <!DOCTYPE html>
    <html>
    <head><meta charset="UTF-8"></head>
    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
        <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
            <div style="background: linear-gradient(135deg, #00b894 0%, #00d2d3 100%); padding: 30px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 28px;">🛒 Sipariş Onayı</h1>
                <p style="color: rgba(255,255,255,0.9); margin: 10px 0 0 0;">Sipariş No: #{siparis.siparis_id}</p>
            </div>
            <div style="padding: 30px;">
                <p style="font-size: 18px; color: #333;">Merhaba <strong>{siparis.musteri_ad}</strong>,</p>
                <p style="color: #666;">Siparişiniz başarıyla alındı!</p>
                
                <div style="background: #e8f5e9; border-radius: 8px; padding: 15px; margin: 20px 0;">
                    <p style="margin: 5px 0;"><strong>🏪 Mağaza:</strong> {siparis.magaza_ad}</p>
                    <p style="margin: 5px 0;"><strong>📅 Tarih:</strong> {siparis.siparis_tarihi}</p>
                </div>
                
                <h3 style="color: #333; border-bottom: 2px solid #00b894; padding-bottom: 10px;">📦 Sipariş Kalemleri</h3>
                <table style="width: 100%; border-collapse: collapse; margin: 15px 0;">
                    <thead>
                        <tr style="background: #f8f9fa;">
                            <th style="padding: 12px; text-align: left; border-bottom: 2px solid #ddd;">Ürün</th>
                            <th style="padding: 12px; text-align: center; border-bottom: 2px solid #ddd;">Beden</th>
                            <th style="padding: 12px; text-align: center; border-bottom: 2px solid #ddd;">Adet</th>
                            <th style="padding: 12px; text-align: right; border-bottom: 2px solid #ddd;">Fiyat</th>
                        </tr>
                    </thead>
                    <tbody>
                        {kalemler_html}
                    </tbody>
                    <tfoot>
                        <tr style="background: #e8f5e9; font-weight: bold;">
                            <td colspan="3" style="padding: 15px; text-align: right;">TOPLAM:</td>
                            <td style="padding: 15px; text-align: right; color: #00b894; font-size: 20px;">{siparis.toplam_tutar:.2f} TL</td>
                        </tr>
                    </tfoot>
                </table>
                
                <div style="background: #fff3e0; border-radius: 8px; padding: 15px; margin: 20px 0;">
                    <h4 style="margin: 0 0 10px 0; color: #ff9800;">📍 Teslimat Adresi</h4>
                    <p style="margin: 0; color: #666;">{siparis.teslimat_adresi}</p>
                </div>
            </div>
            <div style="background: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #eee;">
                <p style="color: #999; font-size: 12px; margin: 0;">
                    © 2026 Mağaza Sistemi | Python Microservice ile gönderildi
                </p>
            </div>
        </div>
    </body>
    </html>
    """

# ============ API ENDPOINTS ============

@app.get("/")
def root():
    """API durumu kontrolü"""
    return {"status": "running", "service": "Email Microservice", "version": "1.0.0"}

@app.get("/health")
def health():
    """Health check endpoint"""
    return {"status": "healthy", "mail_configured": bool(MAIL_USER and MAIL_PASS)}

@app.post("/api/email/send", response_model=EmailResponse)
def send_generic_email(request: EmailRequest):
    """Genel email gönderimi"""
    success = send_email(request.to, request.subject, request.body)
    if success:
        return EmailResponse(success=True, message="Email başarıyla gönderildi")
    raise HTTPException(status_code=500, detail="Email gönderilemedi")

@app.post("/api/email/welcome", response_model=EmailResponse)
def send_welcome_email(request: WelcomeEmailRequest):
    """Hoşgeldin emaili gönder - Kayıt olunca Spring Boot buraya istek atar"""
    html = get_welcome_html(request.kullanici_adi, request.ad)
    success = send_email(request.to, "🎉 Mağaza Sistemine Hoş Geldiniz!", html)
    if success:
        return EmailResponse(success=True, message="Hoşgeldin emaili gönderildi")
    raise HTTPException(status_code=500, detail="Email gönderilemedi")

@app.post("/api/email/order", response_model=EmailResponse)
def send_order_email(request: SiparisOzetiRequest):
    """Sipariş özeti emaili gönder - Sipariş tamamlanınca Spring Boot buraya istek atar"""
    html = get_order_html(request)
    success = send_email(request.to, f"🛒 Sipariş Onayı - #{request.siparis_id}", html)
    if success:
        return EmailResponse(success=True, message="Sipariş özeti emaili gönderildi")
    raise HTTPException(status_code=500, detail="Email gönderilemedi")

# ============ MAIN ============

if __name__ == "__main__":
    import uvicorn
    print("🚀 Email Microservice başlatılıyor...")
    print(f"📧 Mail User: {MAIL_USER}")
    print(f"🔐 Mail Pass: {'*' * len(MAIL_PASS) if MAIL_PASS else 'NOT SET'}")
    uvicorn.run(app, host="0.0.0.0", port=8000)
