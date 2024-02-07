ALTER TABLE member
ADD COLUMN profile_image_id VARCHAR(36);

ALTER TABLE member
ADD CONSTRAINT profile_image_id_uniq
UNIQUE (profile_image_id);