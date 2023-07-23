use documents;

db.createCollection('fs.chunks');
db.createCollection('fs.files');

db.fs.files.createIndex({ filename: 1 });