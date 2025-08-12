import com.esclient.ratingservice.repository.Repository;


@Service
public final class Service
{
    private final Repository repository;

    public Service(Repository repository)
    {
        this.repository = repository;
    }

    public int rateMod(long mod_id, long author_id, int rate)
    {
        return (int)repository.addRate(mod_id, author_id, rate);
    }
}