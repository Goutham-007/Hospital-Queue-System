import requests
import datetime

BACKEND_URL = "http://localhost:8080/actuator/health"
FRONTEND_URL = "http://localhost:5173"
LOG_FILE = "health_check.log"

def check_service(name, url):
    try:
        response = requests.get(url, timeout=5)
        status = "UP" if response.status_code == 200 else "DEGRADED"
        print(f"{name}: {status} (HTTP {response.status_code})")
        return status
    except Exception as e:
        print(f"{name}: DOWN - {str(e)}")
        return "DOWN"

def run_health_check():
    timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"\n=== Health Check at {timestamp} ===")

    backend = check_service("Backend API", BACKEND_URL)
    frontend = check_service("Frontend", FRONTEND_URL)

    with open(LOG_FILE, "a") as f:
        f.write(f"{timestamp} | Backend: {backend} | Frontend: {frontend}\n")

    print(f"Log saved to {LOG_FILE}")

if __name__ == "__main__":
    run_health_check()