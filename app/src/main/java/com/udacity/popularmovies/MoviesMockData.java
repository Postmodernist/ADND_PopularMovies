package com.udacity.popularmovies;

class MoviesMockData {

  static Movie[] get() {
    String title;
    String posterUrl;
    String overview;
    int voteAverage;
    long releaseDate;

    Movie[] movies = new Movie[10];

    title = "Interstellar";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BZjdkOTU3MDktN2IxOS00OGEyLWFmMjktY2FiMmZkNWIyODZiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.";
    voteAverage = 74;
    releaseDate = 1415307600;
    movies[0] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "The Matrix";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BNzQzOTk3OTAtNDQ0Zi00ZTVkLWI0MTEtMDllZjNkYzNjNTc4L2ltYWdlXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.";
    voteAverage = 73;
    releaseDate = 922824000;
    movies[1] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "The Lord of the Rings: The Return of the King";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BYWY1ZWQ5YjMtMDE0MS00NWIzLWE1M2YtODYzYTk2OTNlYWZmXkEyXkFqcGdeQXVyNDUyOTg3Njg@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "Gandalf and Aragorn lead the World of Men against Sauron's army to draw his gaze from Frodo and Sam as they approach Mount Doom with the One Ring.";
    voteAverage = 94;
    releaseDate = 1071608400;
    movies[2] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Forrest Gump";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BNWIwODRlZTUtY2U3ZS00Yzg1LWJhNzYtMmZiYmEyNmU1NjMzXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_UY268_CR1,0,182,268_AL_.jpg";
    overview = "The presidencies of Kennedy and Johnson, Vietnam, Watergate, and other history unfold through the perspective of an Alabama man with an IQ of 75.";
    voteAverage = 82;
    releaseDate = 773438400;
    movies[3] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Fight Club";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMzFjMWNhYzQtYTIxNC00ZWQ1LThiOTItNWQyZmMxNDYyMjA5XkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "An insomniac office worker, looking for a way to change his life, crosses paths with a devil-may-care soapmaker, forming an underground fight club that evolves into something much, much more.";
    voteAverage = 66;
    releaseDate = 939931200;
    movies[4] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Pulp Fiction";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMTkxMTA5OTAzMl5BMl5BanBnXkFtZTgwNjA5MDc3NjE@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "The lives of two mob hitmen, a boxer, a gangster's wife, and a pair of diner bandits intertwine in four tales of violence and redemption.";
    voteAverage = 94;
    releaseDate = 782082000;
    movies[5] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "The Dark Knight";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "When the menace known as the Joker emerges from his mysterious past, he wreaks havoc and chaos on the people of Gotham, the Dark Knight must accept one of the greatest psychological and physical tests of his ability to fight injustice.";
    voteAverage = 82;
    releaseDate = 1216324800;
    movies[6] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Inception";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "A thief, who steals corporate secrets through the use of dream-sharing technology, is given the inverse task of planting an idea into the mind of a CEO.";
    voteAverage = 74;
    releaseDate = 1279224000;
    movies[7] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Black Panther";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMTg1MTY2MjYzNV5BMl5BanBnXkFtZTgwMTc4NTMwNDI@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "T'Challa, the King of Wakanda, rises to the throne in the isolated, technologically advanced African nation, but his claim is challenged by a vengeful outsider who was a childhood victim of T'Challa's father's mistake.";
    voteAverage = 88;
    releaseDate = 1518728400;
    movies[8] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    title = "Thor: Ragnarok";
    posterUrl = "https://images-na.ssl-images-amazon.com/images/M/MV5BMjMyNDkzMzI1OF5BMl5BanBnXkFtZTgwODcxODg5MjI@._V1_UX182_CR0,0,182,268_AL_.jpg";
    overview = "Thor is imprisoned on the other side of the universe and finds himself in a race against time to get back to Asgard to stop Ragnarok, the destruction of his homeworld and the end of Asgardian civilization, at the hands of an all-powerful new threat, the ruthless Hela.";
    voteAverage = 74;
    releaseDate = 1509656400;
    movies[9] = new Movie(title, posterUrl, overview, voteAverage, releaseDate);

    return movies;
  }
}
