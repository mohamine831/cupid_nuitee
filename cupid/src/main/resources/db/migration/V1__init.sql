-- Flyway migration: create core tables

CREATE TABLE property (
  hotel_id BIGINT PRIMARY KEY,
  cupid_id BIGINT,
  name TEXT,
  hotel_type TEXT,
  hotel_type_id INTEGER,
  chain TEXT,
  chain_id INTEGER,
  latitude DOUBLE PRECISION,
  longitude DOUBLE PRECISION,
  phone TEXT,
  email TEXT,
  fax TEXT,
  pets_allowed BOOLEAN,
  child_allowed BOOLEAN,
  airport_code TEXT,
  group_room_min INTEGER,
  main_image_th TEXT,
  checkin_json JSON,
  parking TEXT,
  address_json JSON,
  stars INTEGER,
  rating NUMERIC,
  review_count INTEGER,
  description_html TEXT,
  markdown_description TEXT,
  important_info TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE property_translation (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
  lang VARCHAR(10) NOT NULL,
  description_html TEXT,
  markdown_description TEXT,
  fetched_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  UNIQUE(hotel_id, lang)
);

CREATE TABLE property_photo (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
  url TEXT,
  hd_url TEXT,
  image_description TEXT,
  image_class1 TEXT,
  main_photo BOOLEAN,
  score NUMERIC,
  class_id INTEGER,
  class_order INTEGER
);

CREATE TABLE room (
  id BIGINT PRIMARY KEY,
  hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
  room_name TEXT,
  description TEXT,
  room_size_square NUMERIC,
  room_size_unit TEXT,
  max_adults INTEGER,
  max_children INTEGER,
  max_occupancy INTEGER,
  bed_relation TEXT,
  bed_types_json JSON,
  views_json JSON
);

CREATE TABLE room_photo (
  id BIGSERIAL PRIMARY KEY,
  room_id BIGINT REFERENCES room(id) ON DELETE CASCADE,
  url TEXT,
  hd_url TEXT,
  image_description TEXT,
  class_order INTEGER,
  image_class1 VARCHAR(255),
  class_id INTEGER,
  main_photo BOOLEAN,
  score NUMERIC
);

CREATE TABLE review (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
  average_score NUMERIC,
  country VARCHAR(50),
  type TEXT,
  name TEXT,
  review_date TIMESTAMP,
  headline TEXT,
  language VARCHAR(10),
  pros TEXT,
  cons TEXT,
  source TEXT
);

CREATE TABLE property_facility (
  id BIGSERIAL PRIMARY KEY,
  hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
  facility_id INTEGER,
  facility_name TEXT
);

CREATE TABLE policy (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT REFERENCES property(hotel_id) ON DELETE CASCADE,
    policy_type VARCHAR(255),
    name VARCHAR(255),
    description TEXT
);

CREATE TABLE room_amenity (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT REFERENCES room(id) ON DELETE CASCADE,
    amenities_id INTEGER,
    name VARCHAR(255),
    sort INTEGER
);

CREATE INDEX IF NOT EXISTS idx_property_hotel_id ON property(hotel_id);
CREATE INDEX IF NOT EXISTS idx_property_cupid_id ON property(cupid_id);
CREATE INDEX IF NOT EXISTS idx_property_name ON property(name);
