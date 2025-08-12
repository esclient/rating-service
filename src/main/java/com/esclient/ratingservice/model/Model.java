import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "rates")
public class RateModMessage
{
    @Id
    long mod_id;
    long author_id;
    int rate;

    public RateModMessage(long mod_id, long author_id, int rate)
    {
        this.mod_id = mod_id;
        this.author_id = author_id;
        this.rate = rate;
    }
    
    public long getModId()
    {
        return mod_id;
    }

    public long getAuthorId()
    {
        return author_id;
    }

    public int getRate()
    {
        return rate;
    }


}