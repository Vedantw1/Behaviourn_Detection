import os
import random
import numpy as np
import pandas as pd

def generate_genuine_sample(user_id):
    """Simulates natural human typing, scrolling, and mouse navigation."""
    # Mouse kinematics: smooth curves, natural human velocity & acceleration
    avg_mouse_speed = max(20.0, np.random.normal(230.0, 55.0))
    mouse_accel = max(15000.0, np.random.normal(75000.0, 20000.0))
    click_freq = max(0.03, np.random.normal(0.45, 0.20))
    scroll_speed = max(0.0, np.random.normal(0.8, 0.5)) if random.random() > 0.4 else 0.0

    # Keystroke dynamics: normal human dwell (80-130ms) & flight (180-300ms)
    avg_dwell = max(55.0, np.random.normal(102.0, 14.0))
    avg_flight = max(110.0, np.random.normal(235.0, 40.0))
    typing_speed = max(1.2, np.random.normal(3.8, 0.9))
    backspace_count = int(max(0, np.random.poisson(3.2)))

    # Session engagement
    idle_time = max(1.0, min(14.0, np.random.normal(5.5, 2.5)))
    session_duration = 30.0
    window_switches = int(np.random.choice([0, 1, 2], p=[0.7, 0.25, 0.05]))
    active_ratio = round((session_duration - idle_time) / session_duration, 4)

    return {
        'UserID': user_id,
        'Label': 'Genuine',
        'AvgMouseSpeed': round(avg_mouse_speed, 4),
        'MouseAcceleration': round(mouse_accel, 4),
        'ClickFrequency': round(click_freq, 4),
        'ScrollSpeed': round(scroll_speed, 4),
        'AvgDwellTime': round(avg_dwell, 4),
        'AvgFlightTime': round(avg_flight, 4),
        'TypingSpeed': round(typing_speed, 4),
        'BackspaceCount': backspace_count,
        'IdleTime': round(idle_time, 4),
        'SessionDuration': session_duration,
        'WindowSwitchCount': window_switches,
        'ActiveTimeRatio': active_ratio
    }

def generate_fraud_sample(user_id):
    """
    Simulates various fraud profiles:
    1. Automated Bot / Credential Stuffer (superhuman speed, zero flight time, constant dwell)
    2. Auto-Clicker / Brute Force (rapid clicks, unnatural acceleration)
    3. Imposter / Intruder (erratic movement, high hesitation, extreme backspaces)
    """
    profile = random.choice(['bot', 'autoclicker', 'imposter'])

    if profile == 'bot':
        # Superhuman speed, constant timing, zero typos
        avg_mouse_speed = random.choice([0.0, np.random.normal(1200.0, 200.0)]) # teleporting or no mouse
        mouse_accel = 0.0 if avg_mouse_speed == 0.0 else np.random.normal(350000.0, 50000.0)
        click_freq = max(0.0, np.random.normal(0.05, 0.02))
        scroll_speed = 0.0

        avg_dwell = max(10.0, np.random.normal(25.0, 2.0))   # perfectly fixed robotic dwell
        avg_flight = max(0.0, np.random.normal(12.0, 3.0))   # robotic instantaneous key intervals
        typing_speed = max(8.0, np.random.normal(14.0, 2.5)) # inhuman typing speed
        backspace_count = 0                                  # scripts don't make human typos
        idle_time = max(0.0, np.random.normal(0.2, 0.1))
        session_duration = 30.0
        window_switches = 0
        active_ratio = 1.0

    elif profile == 'autoclicker':
        # Extreme click frequencies, high acceleration jitter
        avg_mouse_speed = max(10.0, np.random.normal(40.0, 15.0))
        mouse_accel = max(180000.0, np.random.normal(280000.0, 40000.0))
        click_freq = max(6.0, np.random.normal(15.0, 3.5))   # 10-20 clicks/sec
        scroll_speed = 0.0
        avg_dwell = 0.0
        avg_flight = 0.0
        typing_speed = 0.0
        backspace_count = 0
        idle_time = max(0.1, np.random.normal(1.0, 0.5))
        session_duration = 30.0
        window_switches = 0
        active_ratio = round((session_duration - idle_time) / session_duration, 4)

    else:  # imposter / human intruder
        # Sluggish, high dwell time, abnormal backspaces, high hesitation
        avg_mouse_speed = max(15.0, np.random.normal(90.0, 30.0))
        mouse_accel = max(10000.0, np.random.normal(45000.0, 15000.0))
        click_freq = max(0.02, np.random.normal(0.15, 0.08))
        scroll_speed = max(0.0, np.random.normal(0.3, 0.2))

        avg_dwell = max(140.0, np.random.normal(220.0, 40.0))   # hesitant keystroke hold
        avg_flight = max(350.0, np.random.normal(550.0, 90.0))  # hunting for keys
        typing_speed = max(0.5, np.random.normal(1.4, 0.4))
        backspace_count = int(max(7, np.random.poisson(14)))    # massive typos
        idle_time = max(12.0, min(26.0, np.random.normal(18.0, 3.5))) # high hesitation
        session_duration = 30.0
        window_switches = int(np.random.choice([2, 3, 4, 5]))
        active_ratio = round((session_duration - idle_time) / session_duration, 4)

    return {
        'UserID': user_id,
        'Label': 'Fraud',
        'AvgMouseSpeed': round(avg_mouse_speed, 4),
        'MouseAcceleration': round(mouse_accel, 4),
        'ClickFrequency': round(click_freq, 4),
        'ScrollSpeed': round(scroll_speed, 4),
        'AvgDwellTime': round(avg_dwell, 4),
        'AvgFlightTime': round(avg_flight, 4),
        'TypingSpeed': round(typing_speed, 4),
        'BackspaceCount': backspace_count,
        'IdleTime': round(idle_time, 4),
        'SessionDuration': session_duration,
        'WindowSwitchCount': window_switches,
        'ActiveTimeRatio': active_ratio
    }

def generate_dataset(csv_path="behavioral_data.csv", n_genuine=250, n_fraud=250):
    print("🚀 Generating synthetic behavioral biometrics dataset...")
    
    # Keep any existing real rows if available
    existing_rows = []
    if os.path.exists(csv_path):
        try:
            df_old = pd.read_csv(csv_path)
            if not df_old.empty:
                print(f"📌 Preserving {len(df_old)} existing recorded session rows from {csv_path}")
                existing_rows = df_old.to_dict('records')
        except Exception as e:
            print(f"Warning reading existing CSV: {e}")

    samples = []
    users = ['user_01', 'user_02', 'vedant', 'user_03']

    for _ in range(n_genuine):
        samples.append(generate_genuine_sample(random.choice(users)))

    for _ in range(n_fraud):
        samples.append(generate_fraud_sample('attacker_' + str(random.randint(1, 15))))

    # Combine existing real data + synthetic data
    all_rows = existing_rows + samples
    df_combined = pd.DataFrame(all_rows)

    # Shuffle dataset
    df_combined = df_combined.sample(frac=1.0, random_state=42).reset_index(drop=True)

    # Save to behavioral_data.csv (both root and ml/ if needed)
    df_combined.to_csv(csv_path, index=False)
    if os.path.exists("ml"):
        df_combined.to_csv(os.path.join("ml", "behavioral_data.csv"), index=False)

    print(f"✅ Successfully written {len(df_combined)} total rows to {csv_path}")
    print("\n📊 Class Distribution:")
    print(df_combined['Label'].value_counts())
    print("\n🔍 Sample Preview:")
    print(df_combined[['UserID', 'Label', 'AvgMouseSpeed', 'AvgDwellTime', 'AvgFlightTime', 'TypingSpeed']].head())

if __name__ == "__main__":
    target_path = "../behavioral_data.csv" if os.path.exists("../behavioral_data.csv") else "behavioral_data.csv"
    generate_dataset(target_path, n_genuine=250, n_fraud=250)
