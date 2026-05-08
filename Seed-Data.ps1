# =============================================================================
#  Seed-Data.ps1
#  Seeds 100 users (80 USER, 15 STAFF, 5 ADMIN), 200 books with cover images,
#  and 27 historical loans across 8 dedicated loan-user accounts with realistic
#  borrow dates, return dates, statuses (ACTIVE/RETURNED/OVERDUE), and fines.
#
#  Usage:
#    .\postman\Seed-Data.ps1
#    .\postman\Seed-Data.ps1 -GatewayUrl "http://localhost:8080" -StaffEmail "seed-staff@elibrary.ie" -StaffPassword "Seed@12345"
#
#  Requirements: Docker stack running, internet access for cover images.
# =============================================================================

param(
    [string]$GatewayUrl    = "http://localhost:8080",
    [string]$StaffEmail    = "seed-staff@elibrary.ie",
    [string]$StaffPassword = "Seed@12345"
)

$ErrorActionPreference = "Stop"

function Write-Header([string]$msg) { Write-Host "`n=== $msg ===" -ForegroundColor Cyan }
function Write-Step([string]$msg)   { Write-Host "  >> $msg"    -ForegroundColor Yellow }
function Write-Pass([string]$msg)   { Write-Host "  [OK] $msg"  -ForegroundColor Green }
function Write-Fail([string]$msg)   { Write-Host "  [!!] $msg"  -ForegroundColor Red }
function Write-Info([string]$msg)   { Write-Host "  $msg"       -ForegroundColor Gray }

# ── 200 books ─────────────────────────────────────────────────────────────────
$books = @(
    # ROMANCE (17)
    @{ Title="Pride and Prejudice"; Author="Jane Austen"; Year=1813; Genre="ROMANCE"; Language="ENGLISH"; Description="Elizabeth Bennet navigates love, class, and family in Regency England while sparring with the proud Mr Darcy." }
    @{ Title="Outlander"; Author="Diana Gabaldon"; Year=1991; Genre="ROMANCE"; Language="ENGLISH"; Description="A WWII nurse is transported back to 18th-century Scotland and torn between two very different men." }
    @{ Title="The Notebook"; Author="Nicholas Sparks"; Year=1996; Genre="ROMANCE"; Language="ENGLISH"; Description="A young couple from different social worlds fall in love one summer in 1940s North Carolina." }
    @{ Title="Me Before You"; Author="Jojo Moyes"; Year=2012; Genre="ROMANCE"; Language="ENGLISH"; Description="Louisa Clark becomes caretaker to the fiercely independent Will Traynor and the two change each other forever." }
    @{ Title="Bridget Jones's Diary"; Author="Helen Fielding"; Year=1996; Genre="ROMANCE"; Language="ENGLISH"; Description="Bridget Jones documents one chaotic London year in this razor-sharp modern retelling of Pride and Prejudice." }
    @{ Title="It Ends with Us"; Author="Colleen Hoover"; Year=2016; Genre="ROMANCE"; Language="ENGLISH"; Description="Lily falls for a neurosurgeon who is everything she ever wanted until a past love reappears and everything falls apart." }
    @{ Title="The Hating Game"; Author="Sally Thorne"; Year=2016; Genre="ROMANCE"; Language="ENGLISH"; Description="Two rivals sharing an office must decide whether their constant bickering is hiding something more." }
    @{ Title="Beach Read"; Author="Emily Henry"; Year=2020; Genre="ROMANCE"; Language="ENGLISH"; Description="A romance novelist and a literary fiction author swap genres for the summer and accidentally fall for each other." }
    @{ Title="People We Meet on Vacation"; Author="Emily Henry"; Year=2021; Genre="ROMANCE"; Language="ENGLISH"; Description="Best friends Alex and Poppy weave between past summers and a last-chance trip to save their friendship." }
    @{ Title="The Kiss Quotient"; Author="Helen Hoang"; Year=2018; Genre="ROMANCE"; Language="ENGLISH"; Description="An econometrician with Asperger's hires a male escort to help her practice dating with unexpected feelings." }
    @{ Title="Eleanor Oliphant Is Completely Fine"; Author="Gail Honeyman"; Year=2017; Genre="ROMANCE"; Language="ENGLISH"; Description="Eleanor lives a meticulously planned life alone until two unexpected people shatter her carefully built walls." }
    @{ Title="Anna and the French Kiss"; Author="Stephanie Perkins"; Year=2010; Genre="ROMANCE"; Language="ENGLISH"; Description="Anna is sent to a Paris boarding school for senior year and meets the charming, complicated Etienne St Clair." }
    @{ Title="The Time Traveler's Wife"; Author="Audrey Niffenegger"; Year=2003; Genre="ROMANCE"; Language="ENGLISH"; Description="Henry involuntarily time-travels through his own life and he and his wife Clare must hold their marriage together." }
    @{ Title="Normal People"; Author="Sally Rooney"; Year=2018; Genre="ROMANCE"; Language="ENGLISH"; Description="Connell and Marianne grow up in the same small Irish town yet navigate totally different paths through college." }
    @{ Title="Conversations with Friends"; Author="Sally Rooney"; Year=2017; Genre="ROMANCE"; Language="ENGLISH"; Description="Two Dublin students become entangled with a married couple raising questions about love, power, and artistic ambition." }
    @{ Title="Daisy Jones and The Six"; Author="Taylor Jenkins Reid"; Year=2019; Genre="ROMANCE"; Language="ENGLISH"; Description="An oral history of a 1970s rock band follows the electric, volatile relationship between two lead singers." }
    @{ Title="The Seven Husbands of Evelyn Hugo"; Author="Taylor Jenkins Reid"; Year=2017; Genre="ROMANCE"; Language="ENGLISH"; Description="A reclusive Hollywood icon finally reveals the story of her seven marriages to a young journalist." }

    # FANTASY (17)
    @{ Title="The Name of the Wind"; Author="Patrick Rothfuss"; Year=2007; Genre="FANTASY"; Language="ENGLISH"; Description="Kvothe narrates his extraordinary journey from street orphan to the most notorious wizard his world has ever seen." }
    @{ Title="A Court of Thorns and Roses"; Author="Sarah J. Maas"; Year=2015; Genre="FANTASY"; Language="ENGLISH"; Description="A mortal huntress is taken into a faerie world and drawn into a conflict that could mean life or death for all." }
    @{ Title="The Way of Kings"; Author="Brandon Sanderson"; Year=2010; Genre="FANTASY"; Language="ENGLISH"; Description="On the storm-ravaged world of Roshar, warriors, scholars, and assassins begin a journey that will change the world." }
    @{ Title="The Blade Itself"; Author="Joe Abercrombie"; Year=2006; Genre="FANTASY"; Language="ENGLISH"; Description="A barbarian, an Inquisitor, and a disgraced soldier are thrown together in a quest that strips away every legend." }
    @{ Title="Good Omens"; Author="Terry Pratchett and Neil Gaiman"; Year=1990; Genre="FANTASY"; Language="ENGLISH"; Description="An angel and a demon join forces to stop Armageddon because they have grown rather fond of Earth." }
    @{ Title="The Lies of Locke Lamora"; Author="Scott Lynch"; Year=2006; Genre="FANTASY"; Language="ENGLISH"; Description="A gang of con artists pull impossibly elaborate heists in a city of canals until they cross the wrong people." }
    @{ Title="Circe"; Author="Madeline Miller"; Year=2018; Genre="FANTASY"; Language="ENGLISH"; Description="The witch Circe discovers her powers and defies the gods of Olympus to forge her own legend." }
    @{ Title="The Song of Achilles"; Author="Madeline Miller"; Year=2011; Genre="FANTASY"; Language="ENGLISH"; Description="Patroclus narrates his life alongside Achilles from boyhood exile to the legendary Trojan War." }
    @{ Title="Six of Crows"; Author="Leigh Bardugo"; Year=2015; Genre="FANTASY"; Language="ENGLISH"; Description="A crew of criminals plan an impossible heist in an icy fortress to steal a drug that could change the world." }
    @{ Title="The Night Circus"; Author="Erin Morgenstern"; Year=2011; Genre="FANTASY"; Language="ENGLISH"; Description="Two young magicians are pitted against each other in an elaborate competition set inside a mysterious black-and-white circus." }
    @{ Title="Mistborn: The Final Empire"; Author="Brandon Sanderson"; Year=2006; Genre="FANTASY"; Language="ENGLISH"; Description="A young street thief discovers powers that could help a band of rebels overthrow an immortal god-emperor." }
    @{ Title="The Priory of the Orange Tree"; Author="Samantha Shannon"; Year=2019; Genre="FANTASY"; Language="ENGLISH"; Description="A queen, a dragonrider, and a mage must band together as an ancient evil stirs for the first time in centuries." }
    @{ Title="Jonathan Strange and Mr Norrell"; Author="Susanna Clarke"; Year=2004; Genre="FANTASY"; Language="ENGLISH"; Description="Two very different English magicians attempt to revive magic in the country during the Napoleonic Wars." }
    @{ Title="Piranesi"; Author="Susanna Clarke"; Year=2020; Genre="FANTASY"; Language="ENGLISH"; Description="A man lives alone in a labyrinthine house filled with statues and tides, slowly uncovering its impossible secrets." }
    @{ Title="The Bear and the Nightingale"; Author="Katherine Arden"; Year=2017; Genre="FANTASY"; Language="ENGLISH"; Description="In medieval Russia, a young woman who can see the spirits of old magic fights to save her village from encroaching darkness." }
    @{ Title="An Ember in the Ashes"; Author="Sabaa Tahir"; Year=2015; Genre="FANTASY"; Language="ENGLISH"; Description="A slave and a soldier are drawn together across opposite sides of a brutal empire built on fear." }
    @{ Title="Words of Radiance"; Author="Brandon Sanderson"; Year=2014; Genre="FANTASY"; Language="ENGLISH"; Description="Kaladin and Shallan journey to the shattered Plains as revelations of an ancient order's return come to light." }

    # MEMOIR (14)
    @{ Title="Educated"; Author="Tara Westover"; Year=2018; Genre="MEMOIR"; Language="ENGLISH"; Description="A woman who grew up in a survivalist family in the mountains of Idaho recounts her journey to Cambridge University." }
    @{ Title="When Breath Becomes Air"; Author="Paul Kalanithi"; Year=2016; Genre="MEMOIR"; Language="ENGLISH"; Description="A neurosurgeon diagnosed with terminal cancer reflects on what makes life meaningful." }
    @{ Title="I Know Why the Caged Bird Sings"; Author="Maya Angelou"; Year=1969; Genre="MEMOIR"; Language="ENGLISH"; Description="Maya Angelou describes her early years growing up in the South, overcoming trauma to find her voice." }
    @{ Title="The Glass Castle"; Author="Jeannette Walls"; Year=2005; Genre="MEMOIR"; Language="ENGLISH"; Description="A vivid portrait of an unconventional and often chaotic childhood with brilliant but deeply flawed parents." }
    @{ Title="Just Kids"; Author="Patti Smith"; Year=2010; Genre="MEMOIR"; Language="ENGLISH"; Description="Patti Smith chronicles her friendship with Robert Mapplethorpe in late 1960s New York." }
    @{ Title="A Long Way Gone"; Author="Ishmael Beah"; Year=2007; Genre="MEMOIR"; Language="ENGLISH"; Description="A former child soldier in Sierra Leone recounts his harrowing journey and eventual path to healing." }
    @{ Title="The Diary of a Young Girl"; Author="Anne Frank"; Year=1947; Genre="MEMOIR"; Language="ENGLISH"; Description="Anne Frank's diary kept during two years hiding in Nazi-occupied Amsterdam." }
    @{ Title="Born a Crime"; Author="Trevor Noah"; Year=2016; Genre="MEMOIR"; Language="ENGLISH"; Description="Trevor Noah recounts his childhood in apartheid South Africa and the crime he was born into just by existing." }
    @{ Title="Becoming"; Author="Michelle Obama"; Year=2018; Genre="MEMOIR"; Language="ENGLISH"; Description="The former First Lady shares her journey from Chicago's South Side to the White House." }
    @{ Title="H Is for Hawk"; Author="Helen Macdonald"; Year=2014; Genre="MEMOIR"; Language="ENGLISH"; Description="A falconer grieves the death of her father by training a goshawk, weaving in the story of T H White." }
    @{ Title="The Year of Magical Thinking"; Author="Joan Didion"; Year=2005; Genre="MEMOIR"; Language="ENGLISH"; Description="A writer explores grief after the sudden death of her husband and the near-death of her daughter." }
    @{ Title="Hillbilly Elegy"; Author="J.D. Vance"; Year=2016; Genre="MEMOIR"; Language="ENGLISH"; Description="A memoir of growing up in a poor Appalachian family and the cultural crisis of working-class America." }
    @{ Title="Bad Blood"; Author="John Carreyrou"; Year=2018; Genre="MEMOIR"; Language="ENGLISH"; Description="The expose of Theranos and its founder Elizabeth Holmes, one of Silicon Valley's greatest frauds." }
    @{ Title="Know My Name"; Author="Chanel Miller"; Year=2019; Genre="MEMOIR"; Language="ENGLISH"; Description="The survivor in the Stanford assault case reclaims her identity in this powerful memoir." }

    # CHILDREN (14)
    @{ Title="Charlotte's Web"; Author="E.B. White"; Year=1952; Genre="CHILDREN"; Language="ENGLISH"; Description="A pig named Wilbur faces slaughter until his best friend Charlotte the spider hatches an extraordinary plan to save him." }
    @{ Title="The Very Hungry Caterpillar"; Author="Eric Carle"; Year=1969; Genre="CHILDREN"; Language="ENGLISH"; Description="A caterpillar eats through a variety of foods before settling into his cocoon and emerging as a butterfly." }
    @{ Title="Where the Wild Things Are"; Author="Maurice Sendak"; Year=1963; Genre="CHILDREN"; Language="ENGLISH"; Description="Max is sent to his room without supper and imagines sailing to a land of wild creatures where he becomes king." }
    @{ Title="The Lion, the Witch and the Wardrobe"; Author="C.S. Lewis"; Year=1950; Genre="CHILDREN"; Language="ENGLISH"; Description="Four siblings step through a wardrobe into the magical world of Narnia where they must defeat the White Witch." }
    @{ Title="Matilda"; Author="Roald Dahl"; Year=1988; Genre="CHILDREN"; Language="ENGLISH"; Description="A brilliant small girl with magical powers outwits her awful parents and terrifying headmistress." }
    @{ Title="The Gruffalo"; Author="Julia Donaldson"; Year=1999; Genre="CHILDREN"; Language="ENGLISH"; Description="A clever mouse invents a monster called the Gruffalo to scare off predators, then discovers it is real." }
    @{ Title="Goodnight Moon"; Author="Margaret Wise Brown"; Year=1947; Genre="CHILDREN"; Language="ENGLISH"; Description="A bedtime classic in which a young bunny says goodnight to everything in the great green room." }
    @{ Title="Green Eggs and Ham"; Author="Dr. Seuss"; Year=1960; Genre="CHILDREN"; Language="ENGLISH"; Description="Sam-I-Am pesters a grouch to try green eggs and ham in increasingly inventive places." }
    @{ Title="James and the Giant Peach"; Author="Roald Dahl"; Year=1961; Genre="CHILDREN"; Language="ENGLISH"; Description="Orphaned James discovers a gigantic magical peach and travels inside it with a cast of oversized insects." }
    @{ Title="A Bear Called Paddington"; Author="Michael Bond"; Year=1958; Genre="CHILDREN"; Language="ENGLISH"; Description="A well-mannered bear from Peru is found at Paddington Station and is taken in by the Brown family." }
    @{ Title="Fantastic Mr Fox"; Author="Roald Dahl"; Year=1970; Genre="CHILDREN"; Language="ENGLISH"; Description="Mr Fox outwits three horrible farmers to feed his family in this beloved Dahl tale." }
    @{ Title="The Phantom Tollbooth"; Author="Norton Juster"; Year=1961; Genre="CHILDREN"; Language="ENGLISH"; Description="A bored boy drives through a magical tollbooth and ends up on a quest to rescue Rhyme and Reason." }
    @{ Title="Coraline"; Author="Neil Gaiman"; Year=2002; Genre="CHILDREN"; Language="ENGLISH"; Description="Coraline discovers a secret door in her new home that leads to a parallel world that seems perfect until it is not." }
    @{ Title="The Secret Garden"; Author="Frances Hodgson Burnett"; Year=1911; Genre="CHILDREN"; Language="ENGLISH"; Description="A spoiled orphan girl discovers a hidden garden and helps restore it and the people around her to life." }

    # YA (15)
    @{ Title="The Hunger Games"; Author="Suzanne Collins"; Year=2008; Genre="YA"; Language="ENGLISH"; Description="In a dystopian North America, sixteen-year-old Katniss volunteers for a televised fight to the death to save her sister." }
    @{ Title="Divergent"; Author="Veronica Roth"; Year=2011; Genre="YA"; Language="ENGLISH"; Description="In a future Chicago, Beatrice Prior discovers she does not fit neatly into any societal faction and that is dangerous." }
    @{ Title="The Maze Runner"; Author="James Dashner"; Year=2009; Genre="YA"; Language="ENGLISH"; Description="Thomas wakes with no memories in a maze populated only by teenage boys and must uncover the mysterious Creators." }
    @{ Title="The Fault in Our Stars"; Author="John Green"; Year=2012; Genre="YA"; Language="ENGLISH"; Description="Two teenagers with cancer meet at a support group and fall in love on a journey to Amsterdam." }
    @{ Title="Looking for Alaska"; Author="John Green"; Year=2005; Genre="YA"; Language="ENGLISH"; Description="Miles heads to boarding school and is driven by the brilliant and unpredictable Alaska Young." }
    @{ Title="Harry Potter and the Philosopher's Stone"; Author="J.K. Rowling"; Year=1997; Genre="YA"; Language="ENGLISH"; Description="An orphaned boy discovers on his eleventh birthday that he is a wizard and departs for Hogwarts School." }
    @{ Title="The Perks of Being a Wallflower"; Author="Stephen Chbosky"; Year=1999; Genre="YA"; Language="ENGLISH"; Description="A shy teenager writes anonymous letters describing his first year of high school and confronting buried trauma." }
    @{ Title="To All the Boys I've Loved Before"; Author="Jenny Han"; Year=2014; Genre="YA"; Language="ENGLISH"; Description="Lara Jean's secret love letters are accidentally mailed out, turning her world completely upside down." }
    @{ Title="The Hate U Give"; Author="Angie Thomas"; Year=2017; Genre="YA"; Language="ENGLISH"; Description="Starr witnesses the fatal shooting of her best friend by a police officer and must find her voice." }
    @{ Title="Children of Blood and Bone"; Author="Tomi Adeyemi"; Year=2018; Genre="YA"; Language="ENGLISH"; Description="Zelie attempts to restore magic to the land of Orisha before the crown prince destroys it forever." }
    @{ Title="Thirteen Reasons Why"; Author="Jay Asher"; Year=2007; Genre="YA"; Language="ENGLISH"; Description="Clay Jensen discovers a box of cassette tapes from his classmate Hannah Baker explaining why she ended her life." }
    @{ Title="The Giver"; Author="Lois Lowry"; Year=1993; Genre="YA"; Language="ENGLISH"; Description="Jonas lives in a seemingly perfect community until he is selected to learn the dark secrets of the world's past." }
    @{ Title="Ender's Game"; Author="Orson Scott Card"; Year=1985; Genre="YA"; Language="ENGLISH"; Description="A gifted child is recruited to a battle school to train as the commander Earth will need to fight aliens." }
    @{ Title="The Book Thief"; Author="Markus Zusak"; Year=2005; Genre="YA"; Language="ENGLISH"; Description="Narrated by Death, a German girl steals books to cope with the horrors of World War II." }
    @{ Title="Shadow and Bone"; Author="Leigh Bardugo"; Year=2012; Genre="YA"; Language="ENGLISH"; Description="Alina Starkov discovers hidden powers in a fractured empire and is drawn into the Darkling's dangerous world." }

    # TRUECRIME (13)
    @{ Title="In Cold Blood"; Author="Truman Capote"; Year=1966; Genre="TRUECRIME"; Language="ENGLISH"; Description="A riveting reconstruction of the brutal murder of a Kansas farm family and the investigation that followed." }
    @{ Title="I'll Be Gone in the Dark"; Author="Michelle McNamara"; Year=2018; Genre="TRUECRIME"; Language="ENGLISH"; Description="An obsessive amateur investigator hunts the Golden State Killer through decades of cold-case evidence." }
    @{ Title="Mindhunter"; Author="John E. Douglas"; Year=1995; Genre="TRUECRIME"; Language="ENGLISH"; Description="The FBI pioneer who coined the term serial killer recounts his interviews with the most dangerous criminals." }
    @{ Title="The Devil in the White City"; Author="Erik Larson"; Year=2003; Genre="TRUECRIME"; Language="ENGLISH"; Description="The 1893 World's Fair in Chicago unfolds alongside the murders committed by Dr H.H. Holmes in his sinister hotel." }
    @{ Title="Say Nothing"; Author="Patrick Radden Keefe"; Year=2018; Genre="TRUECRIME"; Language="ENGLISH"; Description="The unsolved 1972 abduction of Jean McConville haunts the lives of IRA operatives decades later." }
    @{ Title="The Stranger Beside Me"; Author="Ann Rule"; Year=1980; Genre="TRUECRIME"; Language="ENGLISH"; Description="A crime writer who worked alongside Ted Bundy reconstructs her growing horror as she realises who he was." }
    @{ Title="Lost Girls"; Author="Robert Kolker"; Year=2013; Genre="TRUECRIME"; Language="ENGLISH"; Description="The lives of five young women found murdered on Long Island are traced to paint a portrait of overlooked victims." }
    @{ Title="American Fire"; Author="Monica Hesse"; Year=2017; Genre="TRUECRIME"; Language="ENGLISH"; Description="Nearly 100 fires torch an isolated Virginia county over one year, devastating a community already on the edge." }
    @{ Title="Catch and Kill"; Author="Ronan Farrow"; Year=2019; Genre="TRUECRIME"; Language="ENGLISH"; Description="Farrow uncovers how Harvey Weinstein and networks of enablers worked to silence women and journalists." }
    @{ Title="Killers of the Flower Moon"; Author="David Grann"; Year=2017; Genre="TRUECRIME"; Language="ENGLISH"; Description="The systematic murders of Osage Nation members in 1920s Oklahoma exposed a vast, shocking conspiracy." }
    @{ Title="Under the Banner of Heaven"; Author="Jon Krakauer"; Year=2003; Genre="TRUECRIME"; Language="ENGLISH"; Description="The brutal murder of a woman and her daughter by fundamentalist Mormon brothers raises questions of faith and violence." }
    @{ Title="Columbine"; Author="Dave Cullen"; Year=2009; Genre="TRUECRIME"; Language="ENGLISH"; Description="A comprehensive account of the Columbine school shooting, reconstructed over ten years of research." }
    @{ Title="The Feather Thief"; Author="Kirk Wallace Johnson"; Year=2018; Genre="TRUECRIME"; Language="ENGLISH"; Description="A prodigy flute player steals irreplaceable Victorian bird specimens to sell to fly-tying enthusiasts." }

    # SCIFI (16)
    @{ Title="Dune"; Author="Frank Herbert"; Year=1965; Genre="SCIFI"; Language="ENGLISH"; Description="Paul Atreides inherits stewardship of a desert planet that holds the most precious substance in the universe." }
    @{ Title="The Hitchhiker's Guide to the Galaxy"; Author="Douglas Adams"; Year=1979; Genre="SCIFI"; Language="ENGLISH"; Description="Moments before Earth is demolished for a bypass, Arthur Dent is swept into a thoroughly unimpressive universe." }
    @{ Title="Ender's Shadow"; Author="Orson Scott Card"; Year=1999; Genre="SCIFI"; Language="ENGLISH"; Description="Bean's perspective on Battle School reveals a chess-master mind quietly running parallel to Ender's story." }
    @{ Title="Neuromancer"; Author="William Gibson"; Year=1984; Genre="SCIFI"; Language="ENGLISH"; Description="A washed-up computer hacker is recruited for a last-chance mission inside a cyberspace matrix." }
    @{ Title="The Martian"; Author="Andy Weir"; Year=2011; Genre="SCIFI"; Language="ENGLISH"; Description="An astronaut is accidentally left behind on Mars and must science his way through survival." }
    @{ Title="Project Hail Mary"; Author="Andy Weir"; Year=2021; Genre="SCIFI"; Language="ENGLISH"; Description="A lone astronaut wakes with no memories millions of miles from Earth and must save the solar system." }
    @{ Title="Flowers for Algernon"; Author="Daniel Keyes"; Year=1966; Genre="SCIFI"; Language="ENGLISH"; Description="Charlie Gordon, a man with an intellectual disability, undergoes experimental surgery to triple his intelligence." }
    @{ Title="The Left Hand of Darkness"; Author="Ursula K. Le Guin"; Year=1969; Genre="SCIFI"; Language="ENGLISH"; Description="An envoy travels to a planet of ambisexual beings and confronts ideas about gender, loyalty, and humanity." }
    @{ Title="Childhood's End"; Author="Arthur C. Clarke"; Year=1953; Genre="SCIFI"; Language="ENGLISH"; Description="Alien overlords arrive and usher in a utopia but their benevolence hides an unsettling purpose." }
    @{ Title="Ready Player One"; Author="Ernest Cline"; Year=2011; Genre="SCIFI"; Language="ENGLISH"; Description="In 2044, most of humanity escapes to a virtual universe called OASIS where one treasure hunt could change everything." }
    @{ Title="Do Androids Dream of Electric Sheep"; Author="Philip K. Dick"; Year=1968; Genre="SCIFI"; Language="ENGLISH"; Description="In a post-apocalyptic world, a bounty hunter tracks down androids so convincing they believe they are human." }
    @{ Title="The War of the Worlds"; Author="H.G. Wells"; Year=1898; Genre="SCIFI"; Language="ENGLISH"; Description="A Martian invasion of Earth is recounted by the unnamed narrator as civilisation crumbles around him." }
    @{ Title="Hyperion"; Author="Dan Simmons"; Year=1989; Genre="SCIFI"; Language="ENGLISH"; Description="Seven pilgrims journey to the Time Tombs of Hyperion, each carrying a story that may explain the fate of humanity." }
    @{ Title="Brave New World"; Author="Aldous Huxley"; Year=1932; Genre="SCIFI"; Language="ENGLISH"; Description="A perfectly conditioned society built on pleasure and conformity is upended by a man raised outside its walls." }
    @{ Title="Slaughterhouse-Five"; Author="Kurt Vonnegut"; Year=1969; Genre="SCIFI"; Language="ENGLISH"; Description="Billy Pilgrim becomes unstuck in time and relives the Dresden fire-bombing alongside alien abductions." }
    @{ Title="Oryx and Crake"; Author="Margaret Atwood"; Year=2003; Genre="SCIFI"; Language="ENGLISH"; Description="Snowman reflects on the apocalyptic events that wiped out humanity and the roles played by his two best friends." }

    # THRILLER (16)
    @{ Title="Gone Girl"; Author="Gillian Flynn"; Year=2012; Genre="THRILLER"; Language="ENGLISH"; Description="On the morning of their fifth anniversary, Amy Dunne disappears and all eyes turn to her husband Nick." }
    @{ Title="The Girl with the Dragon Tattoo"; Author="Stieg Larsson"; Year=2005; Genre="THRILLER"; Language="ENGLISH"; Description="A journalist and a hacker investigate the decades-old disappearance of a woman from a wealthy Swedish family." }
    @{ Title="The Da Vinci Code"; Author="Dan Brown"; Year=2003; Genre="THRILLER"; Language="ENGLISH"; Description="A murder inside the Louvre Museum reveals a battle between the Priory of Sion and Opus Dei." }
    @{ Title="Big Little Lies"; Author="Liane Moriarty"; Year=2014; Genre="THRILLER"; Language="ENGLISH"; Description="Three women's school-run rivalry escalates to a violent confrontation at a trivia night fundraiser." }
    @{ Title="The Silent Patient"; Author="Alex Michaelides"; Year=2019; Genre="THRILLER"; Language="ENGLISH"; Description="A famous painter shoots her husband and then never speaks again - and a criminal psychotherapist is obsessed with why." }
    @{ Title="The Woman in the Window"; Author="A.J. Finn"; Year=2018; Genre="THRILLER"; Language="ENGLISH"; Description="An agoraphobic woman believes she witnesses a crime through her window but nobody will believe her." }
    @{ Title="Behind Closed Doors"; Author="B.A. Paris"; Year=2016; Genre="THRILLER"; Language="ENGLISH"; Description="Jack and Grace Angel appear to have the perfect marriage but why has Grace never been seen alone?" }
    @{ Title="Then She Was Gone"; Author="Lisa Jewell"; Year=2017; Genre="THRILLER"; Language="ENGLISH"; Description="Laurel Mack's daughter disappeared ten years ago; now she meets a man whose daughter looks uncannily familiar." }
    @{ Title="The Chain"; Author="Adrian McKinty"; Year=2019; Genre="THRILLER"; Language="ENGLISH"; Description="A mother receives a call saying her child has been kidnapped and she must kidnap someone else's child to free her." }
    @{ Title="Verity"; Author="Colleen Hoover"; Year=2018; Genre="THRILLER"; Language="ENGLISH"; Description="A struggling writer discovers an unpublished autobiography that reveals dark secrets about the woman whose life she is writing." }
    @{ Title="The Hunting Party"; Author="Lucy Foley"; Year=2019; Genre="THRILLER"; Language="ENGLISH"; Description="A group of Oxford friends gathering in the remote Scottish Highlands for New Year - one does not make it out alive." }
    @{ Title="I Am Pilgrim"; Author="Terry Hayes"; Year=2013; Genre="THRILLER"; Language="ENGLISH"; Description="A retired spy must track down a terrorist planning a weapon of mass destruction before it is too late." }
    @{ Title="The Maid"; Author="Nita Prose"; Year=2022; Genre="THRILLER"; Language="ENGLISH"; Description="Hotel maid Molly Gray discovers a dead body in a suite and must clear her name in a web of crime." }
    @{ Title="The Thursday Murder Club"; Author="Richard Osman"; Year=2020; Genre="THRILLER"; Language="ENGLISH"; Description="Four unlikely friends in a retirement village meet weekly to solve cold cases until they stumble onto a live one." }
    @{ Title="The Plot"; Author="Jean Hanff Korelitz"; Year=2021; Genre="THRILLER"; Language="ENGLISH"; Description="A writing teacher steals a dead student's unbeatable plot and publishes it, then someone knows." }
    @{ Title="Holly"; Author="Stephen King"; Year=2023; Genre="THRILLER"; Language="ENGLISH"; Description="Holly Gibney investigates several mysterious disappearances in a college town as a serial killer hunts nearby." }

    # SELFHELP (12)
    @{ Title="Atomic Habits"; Author="James Clear"; Year=2018; Genre="SELFHELP"; Language="ENGLISH"; Description="A practical guide to building good habits and breaking bad ones through tiny, sustainable changes." }
    @{ Title="The Subtle Art of Not Giving a Fck"; Author="Mark Manson"; Year=2016; Genre="SELFHELP"; Language="ENGLISH"; Description="A counterintuitive approach to living a good life by focusing only on what truly matters." }
    @{ Title="Man's Search for Meaning"; Author="Viktor E. Frankl"; Year=1946; Genre="SELFHELP"; Language="ENGLISH"; Description="A psychiatrist's account of surviving the Holocaust and the logotherapy philosophy he derived from it." }
    @{ Title="How to Win Friends and Influence People"; Author="Dale Carnegie"; Year=1936; Genre="SELFHELP"; Language="ENGLISH"; Description="Timeless principles for dealing with people, handling criticism, and becoming a better communicator." }
    @{ Title="Think and Grow Rich"; Author="Napoleon Hill"; Year=1937; Genre="SELFHELP"; Language="ENGLISH"; Description="Thirteen principles distilled from interviews with hundreds of successful people including Carnegie and Edison." }
    @{ Title="The 7 Habits of Highly Effective People"; Author="Stephen R. Covey"; Year=1989; Genre="SELFHELP"; Language="ENGLISH"; Description="A framework for personal effectiveness based on character ethics and principle-centred leadership." }
    @{ Title="Daring Greatly"; Author="Brene Brown"; Year=2012; Genre="SELFHELP"; Language="ENGLISH"; Description="Using research on vulnerability, Brown argues that opening up to imperfection is the key to a courageous life." }
    @{ Title="The Power of Now"; Author="Eckhart Tolle"; Year=1997; Genre="SELFHELP"; Language="ENGLISH"; Description="A guide to spiritual enlightenment through living fully in the present moment." }
    @{ Title="Mindset: The New Psychology of Success"; Author="Carol S. Dweck"; Year=2006; Genre="SELFHELP"; Language="ENGLISH"; Description="Psychologist Carol Dweck shows how a growth mindset transforms achievement in every area of life." }
    @{ Title="Deep Work"; Author="Cal Newport"; Year=2016; Genre="SELFHELP"; Language="ENGLISH"; Description="Argues that the ability to focus without distraction is the superpower of the 21st century." }
    @{ Title="The Body Keeps the Score"; Author="Bessel van der Kolk"; Year=2014; Genre="SELFHELP"; Language="ENGLISH"; Description="How trauma reshapes both body and brain along with new treatments that offer real paths to recovery." }
    @{ Title="Ikigai"; Author="Hector Garcia and Francesc Miralles"; Year=2016; Genre="SELFHELP"; Language="ENGLISH"; Description="The Japanese secret to a long, happy life through finding the intersection of passion, mission, vocation, and profession." }

    # HISTORICALFICTION (13)
    @{ Title="Pillars of the Earth"; Author="Ken Follett"; Year=1989; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="The building of a cathedral in 12th-century England weaves together the ambitions of monks, nobles, and craftsmen." }
    @{ Title="All the Light We Cannot See"; Author="Anthony Doerr"; Year=2014; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="A blind French girl and a German soldier's paths converge in occupied France during World War II." }
    @{ Title="The Shadow of the Wind"; Author="Carlos Ruiz Zafon"; Year=2001; Genre="HISTORICALFICTION"; Language="SPANISH"; Description="In post-war Barcelona, a boy discovers a forgotten novel and becomes entangled in its author's mysterious fate." }
    @{ Title="Lincoln in the Bardo"; Author="George Saunders"; Year=2017; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="President Lincoln visits his young son's grave and enters a supernatural realm populated by spirits in transition." }
    @{ Title="Wolf Hall"; Author="Hilary Mantel"; Year=2009; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="Thomas Cromwell rises from blacksmith's son to the right hand of Henry VIII in Tudor England." }
    @{ Title="The Kite Runner"; Author="Khaled Hosseini"; Year=2003; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="A man returns to Taliban-ruled Afghanistan to redeem himself for a terrible act against his childhood best friend." }
    @{ Title="A Thousand Splendid Suns"; Author="Khaled Hosseini"; Year=2007; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="Two Afghan women born a generation apart are bonded together against the backdrop of decades of war." }
    @{ Title="Pachinko"; Author="Min Jin Lee"; Year=2017; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="A Korean family saga spanning generations traces discrimination, ambition, and survival in Japan." }
    @{ Title="The Bronze Horseman"; Author="Paullina Simons"; Year=2000; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="An epic love story set against the siege of Leningrad in World War II begins at the statue of Peter the Great." }
    @{ Title="The Paris Wife"; Author="Paula McLain"; Year=2011; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="Hadley Richardson narrates her marriage to the rising but restless Ernest Hemingway in 1920s Paris." }
    @{ Title="People of the Book"; Author="Geraldine Brooks"; Year=2008; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="A rare-book conservator traces the five-century journey of an illuminated Haggadah across war-torn Europe." }
    @{ Title="The Nightingale"; Author="Kristin Hannah"; Year=2015; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="Two sisters in WWII France take very different paths of resistance against the German occupation." }
    @{ Title="Memoirs of a Geisha"; Author="Arthur Golden"; Year=1997; Genre="HISTORICALFICTION"; Language="ENGLISH"; Description="A young girl from a fishing village is sold into slavery and trained to become a geisha in 1930s Japan." }

    # HISTORICALNF (12)
    @{ Title="Sapiens: A Brief History of Humankind"; Author="Yuval Noah Harari"; Year=2011; Genre="HISTORICALNF"; Language="ENGLISH"; Description="An overview of the history of Homo sapiens from the Stone Age to the political and technological revolutions of the 21st century." }
    @{ Title="Guns, Germs, and Steel"; Author="Jared Diamond"; Year=1997; Genre="HISTORICALNF"; Language="ENGLISH"; Description="An answer to why Western civilisation came to dominate the globe through geography and environment rather than race." }
    @{ Title="The Silk Roads"; Author="Peter Frankopan"; Year=2015; Genre="HISTORICALNF"; Language="ENGLISH"; Description="A retelling of world history with the trade routes connecting East and West at its centre." }
    @{ Title="Team of Rivals"; Author="Doris Kearns Goodwin"; Year=2005; Genre="HISTORICALNF"; Language="ENGLISH"; Description="Lincoln's political genius lay in assembling his most formidable rivals into his cabinet." }
    @{ Title="The Warmth of Other Suns"; Author="Isabel Wilkerson"; Year=2010; Genre="HISTORICALNF"; Language="ENGLISH"; Description="The Great Migration of six million Black Southerners to the North and West told through three individual lives." }
    @{ Title="Unbroken"; Author="Laura Hillenbrand"; Year=2010; Genre="HISTORICALNF"; Language="ENGLISH"; Description="Olympian Louis Zamperini survives a plane crash, 47 days adrift, and a Japanese POW camp in World War II." }
    @{ Title="The Rise and Fall of the Third Reich"; Author="William L. Shirer"; Year=1960; Genre="HISTORICALNF"; Language="ENGLISH"; Description="Eyewitness journalist William Shirer delivers a sweeping account of Nazi Germany from rise to collapse." }
    @{ Title="SPQR: A History of Ancient Rome"; Author="Mary Beard"; Year=2015; Genre="HISTORICALNF"; Language="ENGLISH"; Description="A classicist makes ancient Roman history vivid, questioning what we think we know about the empire." }
    @{ Title="The Splendid and the Vile"; Author="Erik Larson"; Year=2020; Genre="HISTORICALNF"; Language="ENGLISH"; Description="Churchill's first year as Prime Minister told through intimate diaries, letters, and secret documents." }
    @{ Title="Dead Wake"; Author="Erik Larson"; Year=2015; Genre="HISTORICALNF"; Language="ENGLISH"; Description="The final crossing of the Lusitania in 1915, with a German submarine closing in, told in real time." }
    @{ Title="The Romanovs"; Author="Simon Sebag Montefiore"; Year=2016; Genre="HISTORICALNF"; Language="ENGLISH"; Description="Three centuries of the Romanov dynasty told through the family's own letters, diaries, and court records." }
    @{ Title="Empire of Pain"; Author="Patrick Radden Keefe"; Year=2021; Genre="HISTORICALNF"; Language="ENGLISH"; Description="The Sackler dynasty, their pharmaceutical empire, and the opioid crisis they helped create." }

    # NONFICTION (12)
    @{ Title="Thinking, Fast and Slow"; Author="Daniel Kahneman"; Year=2011; Genre="NONFICTION"; Language="ENGLISH"; Description="A psychologist examines two systems of thought and the biases that lead us to make irrational decisions." }
    @{ Title="Freakonomics"; Author="Steven D. Levitt and Stephen J. Dubner"; Year=2005; Genre="NONFICTION"; Language="ENGLISH"; Description="An economist and a journalist apply economic thinking to pop-culture questions with surprising results." }
    @{ Title="The Gene"; Author="Siddhartha Mukherjee"; Year=2016; Genre="NONFICTION"; Language="ENGLISH"; Description="An intimate history of the gene from Mendel to CRISPR and the profound ethical dilemmas it raises." }
    @{ Title="Homo Deus"; Author="Yuval Noah Harari"; Year=2015; Genre="NONFICTION"; Language="ENGLISH"; Description="A brief history of the future exploring what humanity might strive for once disease and war are conquered." }
    @{ Title="The Innovators"; Author="Walter Isaacson"; Year=2014; Genre="NONFICTION"; Language="ENGLISH"; Description="How the digital revolution was produced by a succession of visionaries and the collaborative creative process behind it." }
    @{ Title="Noise: A Flaw in Human Judgment"; Author="Daniel Kahneman"; Year=2021; Genre="NONFICTION"; Language="ENGLISH"; Description="Kahneman reveals how unwanted variability in human judgment degrades decisions across medicine, law, and business." }
    @{ Title="The Righteous Mind"; Author="Jonathan Haidt"; Year=2012; Genre="NONFICTION"; Language="ENGLISH"; Description="A moral psychologist explains why good people are divided by politics and religion." }
    @{ Title="Outliers"; Author="Malcolm Gladwell"; Year=2008; Genre="NONFICTION"; Language="ENGLISH"; Description="An examination of the hidden advantages and extraordinary opportunities that lie behind remarkable success." }
    @{ Title="The Tipping Point"; Author="Malcolm Gladwell"; Year=2000; Genre="NONFICTION"; Language="ENGLISH"; Description="Gladwell examines the moment when a small idea, trend, or social behaviour suddenly crosses the threshold and spreads." }
    @{ Title="When: The Scientific Secrets of Perfect Timing"; Author="Daniel H. Pink"; Year=2018; Genre="NONFICTION"; Language="ENGLISH"; Description="The scientific secrets of perfect timing - when to make decisions, nap, quit, start, and more." }
    @{ Title="Range: Why Generalists Triumph"; Author="David Epstein"; Year=2019; Genre="NONFICTION"; Language="ENGLISH"; Description="Why generalists triumph in a specialised world and the benefits of late starts and diverse experience." }
    @{ Title="The Intelligence Trap"; Author="David Robson"; Year=2019; Genre="NONFICTION"; Language="ENGLISH"; Description="Why smart people make dumb mistakes, and evidence-based strategies for wiser thinking." }
)

# ── Helper: multipart form-data POST ─────────────────────────────────────────
function Invoke-MultipartPost {
    param(
        [string]$Uri,
        [hashtable]$Fields,
        [string]$FilePath,
        [string]$FileField,
        [hashtable]$Headers = @{}
    )

    $boundary  = [System.Guid]::NewGuid().ToString("N")
    $bodyParts = [System.Collections.Generic.List[byte]]::new()
    $enc       = [System.Text.Encoding]::UTF8
    $CRLF      = "`r`n"

    foreach ($key in $Fields.Keys) {
        $val   = $Fields[$key]
        $part  = "--$boundary$CRLF"
        $part += "Content-Disposition: form-data; name=`"$key`"$CRLF$CRLF"
        $part += "$val$CRLF"
        $bodyParts.AddRange($enc.GetBytes($part))
    }

    if ($FilePath -and (Test-Path $FilePath)) {
        $fileBytes   = [System.IO.File]::ReadAllBytes($FilePath)
        $fileName    = [System.IO.Path]::GetFileName($FilePath)
        $partHeader  = "--$boundary$CRLF"
        $partHeader += "Content-Disposition: form-data; name=`"$FileField`"; filename=`"$fileName`"$CRLF"
        $partHeader += "Content-Type: image/jpeg$CRLF$CRLF"
        $bodyParts.AddRange($enc.GetBytes($partHeader))
        $bodyParts.AddRange($fileBytes)
        $bodyParts.AddRange($enc.GetBytes($CRLF))
    }

    $bodyParts.AddRange($enc.GetBytes("--$boundary--$CRLF"))

    $allHeaders = @{ "Content-Type" = "multipart/form-data; boundary=$boundary" } + $Headers

    return Invoke-RestMethod -Uri $Uri -Method POST -Headers $allHeaders -Body $bodyParts.ToArray()
}

# ── Cover image download ──────────────────────────────────────────────────────
$tmpBase = if ($env:TEMP) { $env:TEMP } elseif ($env:TMPDIR) { $env:TMPDIR } else { "/tmp" }
$tmpDir = Join-Path $tmpBase "elibrary-seed-covers"
if (-not (Test-Path $tmpDir)) { New-Item -ItemType Directory -Path $tmpDir | Out-Null }

function Get-CoverImage([string]$title, [string]$author, [int]$index) {
    $safeTitle = $title -replace '[^a-zA-Z0-9]', '_'
    $imgPath   = Join-Path $tmpDir "$index-$safeTitle.jpg"
    if (Test-Path $imgPath) { return $imgPath }

    # Try Open Library
    try {
        $query     = [System.Uri]::EscapeDataString("$title $author")
        $searchUrl = "https://openlibrary.org/search.json?q=$query&fields=cover_i&limit=1"
        $search    = Invoke-RestMethod -Uri $searchUrl -TimeoutSec 10
        $coverId   = $search.docs[0].cover_i
        if ($coverId) {
            $coverUrl = "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
            Invoke-WebRequest -Uri $coverUrl -OutFile $imgPath -TimeoutSec 10 | Out-Null
            if ((Test-Path $imgPath) -and (Get-Item $imgPath).Length -gt 1000) { return $imgPath }
        }
    } catch {}

    # Fallback: placeholder
    try {
        $label    = ($title -replace '[^a-zA-Z0-9 ]','').Substring(0, [Math]::Min(20, $title.Length))
        $encoded  = [System.Uri]::EscapeDataString($label)
        $url      = "https://placehold.co/200x300/6c757d/ffffff/jpg?text=$encoded"
        Invoke-WebRequest -Uri $url -OutFile $imgPath -TimeoutSec 10 | Out-Null
        if ((Test-Path $imgPath) -and (Get-Item $imgPath).Length -gt 500) { return $imgPath }
    } catch {}

    return $null
}

# =============================================================================
#  PART 0 - BOOTSTRAP ADMIN
# =============================================================================
# Registration always creates Role.USER (role escalation was removed).
# We bootstrap one ADMIN directly in the database so subsequent parts can
# use PUT /api/users/{id}/role to assign STAFF and ADMIN roles via the API.

Write-Header "PART 0: Bootstrapping seed admin"

$AdminEmail    = "seed-admin@elibrary.ie"
$AdminPassword = "SeedAdmin@12345"
$adminToken    = $null

Write-Step "Registering bootstrap admin account..."
$adminRegBody = @{ email=$AdminEmail; password=$AdminPassword } | ConvertTo-Json
try {
    $adminReg = Invoke-RestMethod `
        -Uri     "$GatewayUrl/api/auth/register" `
        -Method  POST `
        -Headers @{ "Content-Type"="application/json" } `
        -Body    $adminRegBody
    Write-Pass "Registered bootstrap admin (id=$($adminReg.id))"
} catch {
    $s = [int]$_.Exception.Response.StatusCode
    if ($s -eq 409) { Write-Info "Bootstrap admin already exists - continuing." }
    else { Write-Fail "Bootstrap admin registration failed: $($_.Exception.Message)"; exit 1 }
}

Write-Step "Promoting bootstrap admin to ADMIN role directly in database..."
docker exec postgres psql -U elibrary -d userdb -c "UPDATE users SET role='ADMIN' WHERE email='$AdminEmail'" | Out-Null

Write-Step "Logging in as bootstrap admin..."
$adminLoginBody = @{ email=$AdminEmail; password=$AdminPassword } | ConvertTo-Json
try {
    $adminResp  = Invoke-RestMethod `
        -Uri     "$GatewayUrl/api/auth/login" `
        -Method  POST `
        -Headers @{ "Content-Type"="application/json" } `
        -Body    $adminLoginBody
    $adminToken = $adminResp.token
    Write-Pass "Admin token acquired."
} catch {
    Write-Fail "Admin login failed: $($_.Exception.Message)"; exit 1
}

$adminHeader = @{ "Authorization" = "Bearer $adminToken"; "Content-Type" = "application/json" }

# =============================================================================
#  PART 1 - SEED USERS
# =============================================================================
# All registrations create Role.USER; the admin token is then used to promote
# STAFF and ADMIN accounts via PUT /api/users/{id}/role.

Write-Header "PART 1: Seeding 100 users"

$userCount = 0
$userFail  = 0

$roles = @(
    @{ Role="USER";  Count=80 }
    @{ Role="STAFF"; Count=15 }
    @{ Role="ADMIN"; Count=5  }
)

foreach ($roleGroup in $roles) {
    $role  = $roleGroup.Role
    $count = $roleGroup.Count
    Write-Step "Registering $count $role accounts..."

    for ($i = 1; $i -le $count; $i++) {
        $email = "$($role.ToLower())-$i@elibrary.ie"
        $body  = @{ email=$email; password="Seed@12345" } | ConvertTo-Json

        $registeredId = $null
        try {
            $reg = Invoke-RestMethod `
                -Uri     "$GatewayUrl/api/auth/register" `
                -Method  POST `
                -Headers @{ "Content-Type"="application/json" } `
                -Body    $body
            $registeredId = $reg.id
            $userCount++
        } catch {
            $status = $_.Exception.Response.StatusCode.value__
            if ($status -eq 409) {
                Write-Info "  $email already exists."
            } else {
                Write-Fail "  Failed $email : $($_.Exception.Message)"
                $userFail++
            }
        }

        # Promote to STAFF or ADMIN if needed
        if ($role -ne "USER" -and $registeredId) {
            try {
                $promoteBody = @{ role = $role } | ConvertTo-Json
                Invoke-RestMethod `
                    -Uri     "$GatewayUrl/api/users/$registeredId/role" `
                    -Method  PUT `
                    -Headers $adminHeader `
                    -Body    $promoteBody | Out-Null
            } catch {
                Write-Info "  Could not promote $email to $role : $($_.Exception.Message)"
            }
        }

        Start-Sleep -Milliseconds 50
    }

    Write-Pass "Done $role group."
}

Write-Pass "Users: $userCount created, $userFail errors."

# =============================================================================
#  PART 2 - GET STAFF TOKEN
# =============================================================================
# Register seed-staff as USER (role field no longer accepted at registration),
# then promote to STAFF using the bootstrap admin token from PART 0.

Write-Header "PART 2: Obtaining STAFF JWT"

$staffId   = $null
$staffBody = @{ email=$StaffEmail; password=$StaffPassword } | ConvertTo-Json
try {
    $staffReg = Invoke-RestMethod `
        -Uri     "$GatewayUrl/api/auth/register" `
        -Method  POST `
        -Headers @{ "Content-Type"="application/json" } `
        -Body    $staffBody
    $staffId = $staffReg.id
    Write-Pass "Seed-staff account created (id=$staffId): $StaffEmail"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    if ($status -eq 409) {
        Write-Info "Seed-staff already exists - resolving ID via admin API..."
        try {
            $allUsers = Invoke-RestMethod `
                -Uri     "$GatewayUrl/api/users" `
                -Headers @{ Authorization = "Bearer $adminToken" } `
                -ErrorAction Stop
            $staffUser = $allUsers | Where-Object { $_.email -eq $StaffEmail }
            $staffId   = $staffUser.id
            Write-Info "Resolved id=$staffId"
        } catch {
            Write-Fail "Could not resolve seed-staff ID: $($_.Exception.Message)"; exit 1
        }
    } else {
        Write-Fail "Could not create seed-staff: $($_.Exception.Message)"; exit 1
    }
}

Write-Step "Promoting seed-staff to STAFF role via admin API..."
$promoteBody = @{ role = "STAFF" } | ConvertTo-Json
try {
    Invoke-RestMethod `
        -Uri     "$GatewayUrl/api/users/$staffId/role" `
        -Method  PUT `
        -Headers $adminHeader `
        -Body    $promoteBody | Out-Null
    Write-Pass "Seed-staff promoted to STAFF."
} catch {
    Write-Fail "Could not promote seed-staff: $($_.Exception.Message)"; exit 1
}

$loginBody = @{ email=$StaffEmail; password=$StaffPassword } | ConvertTo-Json
try {
    $loginResp  = Invoke-RestMethod `
        -Uri     "$GatewayUrl/api/auth/login" `
        -Method  POST `
        -Headers @{ "Content-Type"="application/json" } `
        -Body    $loginBody
    $staffToken = $loginResp.token
    Write-Pass "Staff token acquired."
} catch {
    Write-Fail "Login failed: $($_.Exception.Message)"
    exit 1
}

$authHeader = @{ "Authorization" = "Bearer $staffToken" }

# =============================================================================
#  PART 3 - SEED 200 BOOKS
# =============================================================================
Write-Header "PART 3: Seeding $($books.Count) books"

$bookCount   = 0
$bookFail    = 0
$bookSkipped = 0

for ($i = 0; $i -lt $books.Count; $i++) {
    $book = $books[$i]
    $num  = $i + 1

    Write-Step "[$num/$($books.Count)] $($book.Title) ($($book.Genre))"

    $coverPath = Get-CoverImage -title $book.Title -author $book.Author -index $num
    if ($coverPath) { Write-Info "    Cover: $(Split-Path $coverPath -Leaf)" }
    else            { Write-Info "    No cover - submitting without image." }

    $fields = @{
        title         = $book.Title
        author        = $book.Author
        description   = $book.Description
        yearPublished = [string]$book.Year
        genre         = $book.Genre
        language      = $book.Language
    }

    try {
        $null = Invoke-MultipartPost `
            -Uri       "$GatewayUrl/api/books/addTitle" `
            -Fields    $fields `
            -FilePath  $coverPath `
            -FileField "coverImage" `
            -Headers   $authHeader
        Write-Pass "    Added."
        $bookCount++
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($status -eq 409) {
            Write-Info "    Already exists, skipping."
            $bookSkipped++
        } else {
            Write-Fail "    Failed: $($_.Exception.Message)"
            $bookFail++
        }
    }

    Start-Sleep -Milliseconds 200
}

# =============================================================================
#  PART 4 - SEED HISTORICAL LOANS
# =============================================================================
Write-Header "PART 4: Seeding historical loans"

# ── 4a. Create 8 dedicated loan-user accounts ─────────────────────────────────
$loanUsers = @(
    @{ Email="loan-user-alice@elibrary.ie";   Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-bob@elibrary.ie";     Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-carol@elibrary.ie";   Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-dave@elibrary.ie";    Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-eve@elibrary.ie";     Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-frank@elibrary.ie";   Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-grace@elibrary.ie";   Password="Seed@12345"; Role="USER" }
    @{ Email="loan-user-henry@elibrary.ie";   Password="Seed@12345"; Role="USER" }
)

foreach ($lu in $loanUsers) {
    $body = @{ email=$lu.Email; password=$lu.Password; role=$lu.Role } | ConvertTo-Json
    try {
        $null = Invoke-RestMethod -Uri "$GatewayUrl/api/auth/register" -Method POST `
            -Headers @{ "Content-Type"="application/json" } -Body $body
        Write-Pass "Created loan user: $($lu.Email)"
    } catch {
        $sc = $_.Exception.Response.StatusCode.value__
        if ($sc -eq 409) { Write-Info "Loan user already exists: $($lu.Email)" }
        else { Write-Fail "Could not create $($lu.Email): $($_.Exception.Message)" }
    }
}

# ── 4b. Fetch all books to get real IDs ───────────────────────────────────────
Write-Step "Fetching book catalogue..."
$allBooks = @()
try {
    # book-service /api/books/search returns paginated results; use a broad query
    $searchResp = Invoke-RestMethod -Uri "$GatewayUrl/api/books/search?keyword=the" `
        -Method GET -Headers $authHeader
    if ($searchResp -is [array]) { $allBooks = $searchResp }
    elseif ($searchResp.content) { $allBooks = $searchResp.content }
    else { $allBooks = @($searchResp) }
} catch { Write-Info "  Search endpoint error, trying /api/books fallback..." }

# Supplement: try a second broad query to get more IDs
if ($allBooks.Count -lt 20) {
    try {
        $r2 = Invoke-RestMethod -Uri "$GatewayUrl/api/books/search?keyword=a" `
            -Method GET -Headers $authHeader
        $extras = if ($r2 -is [array]) { $r2 } elseif ($r2.content) { $r2.content } else { @($r2) }
        $allBooks = @($allBooks) + @($extras) | Sort-Object id -Unique
    } catch {}
}

$bookIds = @($allBooks | ForEach-Object { $_.id } | Where-Object { $_ -gt 0 } | Select-Object -Unique)
Write-Info "  Found $($bookIds.Count) book IDs."

if ($bookIds.Count -lt 5) {
    Write-Fail "Not enough books to seed loans. Skipping Part 4."
} else {

# ── 4c. Loan scenario definitions ─────────────────────────────────────────────
# Each entry: UserEmail, BookIndex (into $bookIds), DaysAgo, LoanDays, Status, FineEuros
# DaysAgo  = how many days ago the book was borrowed
# LoanDays = loan period (normally 14 days)
# Status   = RETURNED | OVERDUE | ACTIVE
# FineEuros = 0.00 for ACTIVE/on-time RETURNED; >0 for overdue returns
$loanScenarios = @(
    # Alice – returned on time (3 loans over the year)
    @{ User="loan-user-alice@elibrary.ie";  BookIdx=0;  DaysAgo=340; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-alice@elibrary.ie";  BookIdx=1;  DaysAgo=200; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-alice@elibrary.ie";  BookIdx=2;  DaysAgo=5;   LoanDays=14; Status="ACTIVE";   FineEuros=0.00 }

    # Bob – had one overdue return with fine, one currently overdue
    @{ User="loan-user-bob@elibrary.ie";    BookIdx=3;  DaysAgo=280; LoanDays=14; Status="RETURNED"; FineEuros=8.00 }
    @{ User="loan-user-bob@elibrary.ie";    BookIdx=4;  DaysAgo=110; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-bob@elibrary.ie";    BookIdx=5;  DaysAgo=30;  LoanDays=14; Status="OVERDUE";  FineEuros=32.00 }

    # Carol – heavy reader, all returned on time
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=6;  DaysAgo=350; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=7;  DaysAgo=300; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=8;  DaysAgo=250; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=9;  DaysAgo=180; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=10; DaysAgo=90;  LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-carol@elibrary.ie";  BookIdx=11; DaysAgo=10;  LoanDays=14; Status="ACTIVE";   FineEuros=0.00 }

    # Dave – one big fine, currently clear
    @{ User="loan-user-dave@elibrary.ie";   BookIdx=12; DaysAgo=320; LoanDays=14; Status="RETURNED"; FineEuros=20.00 }
    @{ User="loan-user-dave@elibrary.ie";   BookIdx=13; DaysAgo=150; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-dave@elibrary.ie";   BookIdx=14; DaysAgo=60;  LoanDays=14; Status="RETURNED"; FineEuros=4.00 }

    # Eve – recent borrower, one active
    @{ User="loan-user-eve@elibrary.ie";    BookIdx=15; DaysAgo=120; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-eve@elibrary.ie";    BookIdx=16; DaysAgo=7;   LoanDays=14; Status="ACTIVE";   FineEuros=0.00 }

    # Frank – long overdue, two active
    @{ User="loan-user-frank@elibrary.ie";  BookIdx=17; DaysAgo=365; LoanDays=14; Status="RETURNED"; FineEuros=6.00 }
    @{ User="loan-user-frank@elibrary.ie";  BookIdx=18; DaysAgo=45;  LoanDays=14; Status="OVERDUE";  FineEuros=62.00 }
    @{ User="loan-user-frank@elibrary.ie";  BookIdx=19; DaysAgo=3;   LoanDays=14; Status="ACTIVE";   FineEuros=0.00 }

    # Grace – steady reader, one overdue with small fine
    @{ User="loan-user-grace@elibrary.ie";  BookIdx=20; DaysAgo=270; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-grace@elibrary.ie";  BookIdx=21; DaysAgo=200; LoanDays=14; Status="RETURNED"; FineEuros=2.00 }
    @{ User="loan-user-grace@elibrary.ie";  BookIdx=22; DaysAgo=130; LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-grace@elibrary.ie";  BookIdx=23; DaysAgo=20;  LoanDays=14; Status="ACTIVE";   FineEuros=0.00 }

    # Henry – one very late return, one currently overdue
    @{ User="loan-user-henry@elibrary.ie";  BookIdx=24; DaysAgo=310; LoanDays=14; Status="RETURNED"; FineEuros=14.00 }
    @{ User="loan-user-henry@elibrary.ie";  BookIdx=25; DaysAgo=90;  LoanDays=14; Status="RETURNED"; FineEuros=0.00 }
    @{ User="loan-user-henry@elibrary.ie";  BookIdx=26; DaysAgo=25;  LoanDays=14; Status="OVERDUE";  FineEuros=22.00 }
)

# ── 4d. Log in as each loan user and borrow the required books ─────────────────
# Cache: userEmail -> JWT token string
$userTokenCache = @{}

function Get-LoanUserToken([string]$email, [string]$password) {
    if ($userTokenCache.ContainsKey($email)) { return $userTokenCache[$email] }
    $body = @{ email=$email; password=$password } | ConvertTo-Json
    try {
        $r = Invoke-RestMethod -Uri "$GatewayUrl/api/auth/login" -Method POST `
            -Headers @{ "Content-Type"="application/json" } -Body $body
        # LoginResponse contains only { token, expiresAt }
        $userTokenCache[$email] = $r.token
        return $r.token
    } catch {
        Write-Fail "  Login failed for $email : $($_.Exception.Message)"
        return $null
    }
}

$loanCount   = 0
$loanFail    = 0
$loanUpdates = [System.Collections.Generic.List[object]]::new()

foreach ($scenario in $loanScenarios) {
    $bookId = $null
    if ($scenario.BookIdx -lt $bookIds.Count) {
        $bookId = $bookIds[$scenario.BookIdx]
    } else {
        $bookId = $bookIds[$scenario.BookIdx % $bookIds.Count]
    }

    $userToken = Get-LoanUserToken -email $scenario.User -password "Seed@12345"
    if (-not $userToken) { $loanFail++; continue }

    # The gateway injects X-Authenticated-User-Id from the JWT; no need to send it manually
    $borrowBody = @{ bookId=$bookId; email=$scenario.User } | ConvertTo-Json
    try {
        $loanResp = Invoke-RestMethod -Uri "$GatewayUrl/api/loans" -Method POST `
            -Headers @{
                "Authorization" = "Bearer $userToken"
                "Content-Type"  = "application/json"
            } -Body $borrowBody

        $loanId = $loanResp.id
        Write-Pass "  Loan created: $loanId (book $bookId, user $($scenario.User))"

        # Record info needed for the SQL backdate step
        $loanUpdates.Add([pscustomobject]@{
            LoanId    = [string]$loanId
            DaysAgo   = [int]$scenario.DaysAgo
            LoanDays  = [int]$scenario.LoanDays
            Status    = [string]$scenario.Status
            Fine      = [double]$scenario.FineEuros
            UserEmail = [string]$scenario.User
        })
        $loanCount++
    } catch {
        $sc  = $_.Exception.Response.StatusCode.value__
        $msg = $_.Exception.Message
        Write-Fail "  Borrow failed (book $bookId, user $($scenario.User)) [$sc]: $msg"
        $loanFail++
    }

    Start-Sleep -Milliseconds 300
}

Write-Pass "Loans borrowed: $loanCount ok, $loanFail failed."

# ── 4e. Backdate loans via psql ───────────────────────────────────────────────
if ($loanUpdates.Count -gt 0) {
    Write-Step "Backdating $($loanUpdates.Count) loans via psql..."

    $sqlLines = [System.Collections.Generic.List[string]]::new()
    $sqlLines.Add('\connect loandb')
    $sqlLines.Add("SET TimeZone='UTC';")

    foreach ($upd in $loanUpdates) {
        $id       = $upd.LoanId
        $daysAgo  = $upd.DaysAgo
        $loanDays = $upd.LoanDays
        $status   = $upd.Status
        # Use InvariantCulture to ensure period decimal separator for SQL
        $fine     = ([math]::Round($upd.Fine, 2)).ToString([System.Globalization.CultureInfo]::InvariantCulture)

        # borrow_date  = now - daysAgo days (random hour 08-20)
        $randHour = Get-Random -Minimum 8 -Maximum 20
        $borrowSql = "NOW() - INTERVAL '$daysAgo days' + INTERVAL '$randHour hours'"
        $dueSql    = "NOW() - INTERVAL '$daysAgo days' + INTERVAL '$randHour hours' + INTERVAL '$loanDays days'"

        if ($status -eq "RETURNED") {
            # returned somewhere between dueDate-3 days and dueDate+(fine/2) days
            $fineVal      = [double]$fine
            $overdueDays  = [math]::Round($fineVal / 2)   # €2/day rate
            $returnOffset = $daysAgo - $loanDays - $overdueDays
            if ($returnOffset -lt 0) { $returnOffset = 0 }
            $returnSql = "NOW() - INTERVAL '$returnOffset days'"

            $sqlLines.Add("UPDATE loans SET borrow_date=$borrowSql, due_date=$dueSql, return_date=$returnSql, status='RETURNED', fine_amount=$fine WHERE id='$id';")
        } elseif ($status -eq "OVERDUE") {
            $sqlLines.Add("UPDATE loans SET borrow_date=$borrowSql, due_date=$dueSql, return_date=NULL, status='OVERDUE', fine_amount=$fine WHERE id='$id';")
        } else {
            # ACTIVE — just backdate borrow/due, keep status ACTIVE, fine 0
            $sqlLines.Add("UPDATE loans SET borrow_date=$borrowSql, due_date=$dueSql, return_date=NULL, status='ACTIVE', fine_amount=0.00 WHERE id='$id';")
        }
    }

    $sqlScript = $sqlLines -join "`n"

    try {
        $dbUser = "elibrary"
        $result = $sqlScript | docker exec -i postgres psql -U $dbUser 2>&1
        Write-Pass "psql backdate complete."
        Write-Info ($result | Out-String)
    } catch {
        Write-Fail "psql backdate failed: $($_.Exception.Message)"
    }
} # end if loanUpdates.Count

} # end if bookIds.Count

# =============================================================================
#  SUMMARY
# =============================================================================
Write-Header "SEED COMPLETE"
Write-Host "  Users : $userCount created, $userFail errors" -ForegroundColor White
Write-Host "  Books : $bookCount added, $bookSkipped already existed, $bookFail errors" -ForegroundColor White
Write-Host "  Loans : $loanCount borrowed, $loanFail failed" -ForegroundColor White
Write-Host "  Cache : $tmpDir" -ForegroundColor White
