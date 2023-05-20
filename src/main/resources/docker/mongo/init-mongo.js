db.createUser(
    {
       user : "dhs",
       pwd : "dhs",
       roles : [
            {
                role : "readWrite",
                db : "documents"
            }
       ]
    }
)