from locust import HttpUser, task, between

class APIUser(HttpUser):
    # Kullanıcıların iki istek arasında 1 ile 3 saniye beklemesini sağlar
    wait_time = between(1, 3)
    
    def on_start(self):
        """Her simüle edilen kullanıcı (User) başlatıldığında çalışır"""
        self.headers = {}
        # Dispatcher üzerinden Auth servise JWT isteği atarız
        response = self.client.post("/api/auth/login", json={"username": "load_test_user"})
        
        if response.status_code == 200:
            self.token = response.json().get("token")
            # Alınan tokeni global header tanımlamasına ekliyoruz
            self.headers = {"Authorization": f"Bearer {self.token}"}
            print("Login Başarılı! Token Eklendi.")
        else:
            print(f"Login Başarısız! Hata Kodu: {response.status_code}")

    @task(3) # Bu görev daha yüksek ağırlıkta (sıklıkla) çalışır
    def get_users_list(self):
        """User Service (Mikroservis) yük testi"""
        if self.headers:
            self.client.get("/api/users", headers=self.headers, name="Get Users (User Service)")

    @task(2)
    def get_products_list(self):
        """Product Service (Mikroservis) yük testi"""
        if self.headers:
            self.client.get("/api/products", headers=self.headers, name="Get Products (Product Service)")
