package com.tfassbender.mrright.puzzle

/**
 * The fixed name lists all directories are built from: 120 first names × 120 surnames, English and never
 * translated. The order is part of the generator – changing it changes every puzzle (new generator version).
 */
object NameCatalog {
    val firstNames: List<String> = listOf(
        "Albert", "Alfred", "Ambrose", "Andrew", "Anthony", "Arthur", "Augustus", "Barnaby", "Basil", "Benedict",
        "Benjamin", "Bertram", "Cecil", "Charles", "Christopher", "Clarence", "Clement", "Cyril", "Daniel", "David",
        "Desmond", "Douglas", "Duncan", "Edgar", "Edmund", "Edward", "Edwin", "Elias", "Ernest", "Felix",
        "Ferdinand", "Francis", "Frederick", "Gabriel", "Geoffrey", "George", "Gerald", "Gideon", "Gilbert", "Godfrey",
        "Gregory", "Harold", "Henry", "Herbert", "Horace", "Hubert", "Hugh", "Humphrey", "Isaac", "Jacob",
        "James", "Jasper", "Jeremy", "John", "Jonathan", "Joseph", "Julian", "Lawrence", "Leonard", "Lionel",
        "Lucas", "Malcolm", "Marcus", "Matthew", "Maurice", "Miles", "Nathaniel", "Neville", "Nicholas", "Nigel",
        "Noel", "Norman", "Octavius", "Oliver", "Orlando", "Oscar", "Oswald", "Owen", "Patrick", "Percival",
        "Percy", "Peregrine", "Peter", "Philip", "Quentin", "Ralph", "Randolph", "Raymond", "Reginald", "Richard",
        "Robert", "Roderick", "Roland", "Rupert", "Russell", "Samuel", "Sebastian", "Septimus", "Sidney", "Silas",
        "Simon", "Solomon", "Stanley", "Stephen", "Thaddeus", "Theodore", "Thomas", "Timothy", "Tobias", "Tristan",
        "Valentine", "Vernon", "Victor", "Vincent", "Walter", "Wallace", "Wilfred", "William", "Winston", "Zachary",
    )

    val surnames: List<String> = listOf(
        "Abbott", "Ashby", "Ashford", "Atherton", "Barrington", "Beaumont", "Bellamy", "Blackwood", "Bramley", "Brandon",
        "Brooke", "Calloway", "Carlisle", "Carrington", "Chadwick", "Chatterton", "Cole", "Compton", "Crawford", "Dalton",
        "Darby", "Davenport", "Dodd", "Drummond", "Dunmore", "Edge", "Elliot", "Ellsworth", "Everett", "Fairfax",
        "Fanshawe", "Fenwick", "Fielding", "Fitzroy", "Fletcher", "Forsythe", "Garland", "Gilmore", "Grafton", "Granger",
        "Greaves", "Hadley", "Halford", "Harcourt", "Hastings", "Hawthorne", "Holloway", "Huntley", "Ingram", "Irving",
        "Jennings", "Kendall", "Kenward", "Kingsley", "Kirk", "Lambert", "Langley", "Lockwood", "Lovell", "Lytton",
        "Marlowe", "Melbourne", "Merriweather", "Montrose", "Morley", "Napier", "Nash", "Newbury", "Norcott", "Norwood",
        "Oakley", "Ogden", "Osborne", "Pemberton", "Pembroke", "Pickering", "Prescott", "Preston", "Quincey", "Radcliffe",
        "Ramsay", "Ravenscroft", "Redfern", "Rider", "Rochester", "Rothwell", "Rutherford", "Sackville", "Selwyn", "Seymour",
        "Sheridan", "Sinclair", "Stanhope", "Sterling", "Stevens", "Stratton", "Sutton", "Talbot", "Tennant", "Thornbury",
        "Thornton", "Tibbett", "Tremaine", "Tyndall", "Underwood", "Vance", "Vaughan", "Verney", "Wainwright", "Wakefield",
        "Waverley", "Westbrook", "Whitby", "Whitmore", "Winthrop", "Woodville", "Wray", "Yardley", "Yates", "York",
    )
}
