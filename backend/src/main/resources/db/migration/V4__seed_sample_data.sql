-- Urban Intelligence Platform - Seed sample dataset for dashboard visualization

INSERT INTO districts (name, population, sustainability_score, operational_risk_score)
VALUES
    ('Downtown', 85000, 72.5, 45.3),
    ('Midtown', 65000, 68.0, 38.5),
    ('Uptown', 45000, 80.2, 25.1),
    ('Waterfront', 35000, 75.8, 32.0),
    ('Industrial', 25000, 55.0, 62.5)
ON CONFLICT (name) DO NOTHING;

INSERT INTO incidents (type, description, severity, latitude, longitude, district_id, status, created_at, updated_at)
VALUES
    ('Traffic Congestion', 'Heavy traffic on Main St', 'HIGH', 40.7128, -74.0060, (SELECT id FROM districts WHERE name = 'Downtown'), 'IN_PROGRESS', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
    ('Power Outage', 'Electrical outage affecting 3 blocks', 'CRITICAL', 40.7589, -73.9851, (SELECT id FROM districts WHERE name = 'Midtown'), 'REPORTED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    ('Water Leak', 'Major water leak detected', 'MEDIUM', 40.7489, -73.9680, (SELECT id FROM districts WHERE name = 'Downtown'), 'RESOLVED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    ('Gas Leak', 'Possible gas leak at industrial site', 'HIGH', 40.7306, -73.9866, (SELECT id FROM districts WHERE name = 'Industrial'), 'IN_PROGRESS', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    ('Flood Warning', 'Street flooding near waterfront promenade', 'MEDIUM', 40.7060, -74.0090, (SELECT id FROM districts WHERE name = 'Waterfront'), 'REPORTED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days');
