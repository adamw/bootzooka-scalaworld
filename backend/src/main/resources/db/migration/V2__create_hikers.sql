CREATE TABLE "hikers"
(
  "id"              TEXT        NOT NULL,
  "user_id"         TEXT        NOT NULL,
  "route_name"      TEXT        NOT NULL
);
ALTER TABLE "hikers" ADD CONSTRAINT "hikers_id" PRIMARY KEY ("id");
ALTER TABLE "hikers"
  ADD CONSTRAINT "hikers_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
