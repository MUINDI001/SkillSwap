package com.skillswap.app.repository

import com.google.firebase.auth.FirebaseAuth
import com.skillswap.app.model.Swap
import com.skillswap.app.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object FakeRepository {

    private var activeUserId: String = "1"

    private var _usersList = listOf(
        User(
            id = "1",
            name = "Kojo Mensah",
            location = "East Legon, Accra",
            skillsOffered = listOf("Android Development", "Kotlin", "Firebase"),
            skillsWanted = listOf("UI/UX Design", "Photography"),
            rating = 4.9,
            ratingCount = 18,
            bio = "Senior Mobile Engineer based in East Legon, Accra. Passionate about building modern, high-performance Android apps and mentoring local talent.",
            profileImageUrl = "",
            title = "Senior Mobile Engineer",
            email = "kojo.mensah@skillswap.gh",
            phone = "+233 24 123 4567",
            portfolioUrl = "https://github.com/kojomensah",
            certifications = listOf("Google Certified Android Engineer", "Firebase Specialist"),
            yearsOfExperience = 6
        ),
        User(
            id = "2",
            name = "Abena Osei",
            location = "Osu, Accra",
            skillsOffered = listOf("UI/UX Design", "Figma", "Prototyping"),
            skillsWanted = listOf("Android Development", "React"),
            rating = 4.8,
            ratingCount = 14,
            bio = "Lead Product Designer at a tech studio in Osu, Accra. I love creating intuitive, accessible user experiences for African digital products.",
            profileImageUrl = "",
            title = "Lead Product Designer",
            email = "abena.osei@design.gh",
            phone = "+233 20 987 6543",
            portfolioUrl = "https://dribbble.com/abena_ui",
            certifications = listOf("Figma Certified Professional", "UX Design Institute"),
            yearsOfExperience = 4
        ),
        User(
            id = "3",
            name = "Kofi Adomako",
            location = "Labone, Accra",
            skillsOffered = listOf("Photography", "Photo Editing", "Lightroom"),
            skillsWanted = listOf("Digital Marketing", "Social Media"),
            rating = 4.7,
            ratingCount = 11,
            bio = "Professional photographer in Labone specializing in portraits, events, and brand storytelling across Greater Accra.",
            profileImageUrl = "",
            title = "Commercial Photographer",
            email = "kofi@adomakostudios.com",
            phone = "+233 27 555 1234",
            portfolioUrl = "https://adomakostudios.com",
            certifications = listOf("Adobe Certified Expert in Lightroom"),
            yearsOfExperience = 5
        ),
        User(
            id = "4",
            name = "Efua Appiah",
            location = "Madina, Accra",
            skillsOffered = listOf("Graphic Design", "Branding", "Canva"),
            skillsWanted = listOf("Web Development", "Coding"),
            rating = 4.9,
            ratingCount = 22,
            bio = "Brand designer in Madina helping Ghanaian small businesses establish strong visual identities.",
            profileImageUrl = "",
            title = "Brand Identity Specialist",
            email = "efua.appiah@brand.gh",
            phone = "+233 55 444 3322",
            portfolioUrl = "https://behance.net/efuaappiah",
            certifications = listOf("Canva Design Educator"),
            yearsOfExperience = 3
        ),
        User(
            id = "5",
            name = "Kwaku Annan",
            location = "Spintex, Accra",
            skillsOffered = listOf("Guitar", "Music Production", "Piano"),
            skillsWanted = listOf("Twi", "French"),
            rating = 4.8,
            ratingCount = 16,
            bio = "Musician and sound producer on Spintex Road with 10 years of experience. Eager to polish my conversational French.",
            profileImageUrl = "",
            title = "Music Producer & Instructor",
            email = "kwaku.annan@sound.gh",
            phone = "+233 24 888 7766",
            portfolioUrl = "https://soundcloud.com/kwakuannan",
            certifications = listOf("Logic Pro Certified Trainer"),
            yearsOfExperience = 8
        ),
        User(
            id = "6",
            name = "Akosua Gyasi",
            location = "Adenta, Accra",
            skillsOffered = listOf("Cooking", "Ghanaian Cuisine", "Baking"),
            skillsWanted = listOf("Public Speaking", "Business"),
            rating = 4.9,
            ratingCount = 29,
            bio = "Culinary enthusiast in Adenta. I teach authentic Ghanaian dishes including Jollof, Fufu, and gourmet pastries.",
            profileImageUrl = "",
            title = "Culinary Artist & Caterer",
            email = "akosua@gyasikitchen.com",
            phone = "+233 26 333 2211",
            portfolioUrl = "https://gyasikitchen.com",
            certifications = listOf("Culinary Arts Diploma"),
            yearsOfExperience = 7
        ),
        User(
            id = "7",
            name = "Yaw Donkor",
            location = "Kaneshie, Accra",
            skillsOffered = listOf("Business", "Financial Planning", "Excel"),
            skillsWanted = listOf("Digital Marketing", "SEO"),
            rating = 4.6,
            ratingCount = 9,
            bio = "Financial consultant in Kaneshie helping entrepreneurs build sound business plans and financial models.",
            profileImageUrl = "",
            title = "SME Financial Advisor",
            email = "yaw.donkor@bizconsult.gh",
            phone = "+233 23 111 9988",
            portfolioUrl = "",
            certifications = listOf("ACCA Level II", "Financial Modeling Professional"),
            yearsOfExperience = 5
        ),
        User(
            id = "8",
            name = "Esi Koomson",
            location = "Tema, Greater Accra",
            skillsOffered = listOf("Fashion Design", "Pattern Making", "Sewing"),
            skillsWanted = listOf("Social Media", "E-commerce"),
            rating = 4.9,
            ratingCount = 20,
            bio = "Fashion designer in Tema Community 1. I teach contemporary African fashion design and tailoring.",
            profileImageUrl = "",
            title = "Fashion Designer & Tailor",
            email = "esi.koomson@fashion.gh",
            phone = "+233 24 666 5544",
            portfolioUrl = "https://instagram.com/esikoomson_fashion",
            certifications = listOf("Vogue Fashion Institute Certificate"),
            yearsOfExperience = 6
        )
    )

    private val _swaps = MutableStateFlow<List<Swap>>(
        listOf(
            Swap(
                id = "s101",
                fromUser = _usersList[1], // Abena Osei
                toUser = _usersList[0],   // Kojo Mensah
                offeredSkill = "UI/UX Design",
                requestedSkill = "Android Development",
                status = "pending"
            ),
            Swap(
                id = "s102",
                fromUser = _usersList[0], // Kojo Mensah
                toUser = _usersList[2],   // Kofi Adomako
                offeredSkill = "Android Development",
                requestedSkill = "Photography",
                status = "accepted"
            )
        )
    )
    val swaps: StateFlow<List<Swap>> = _swaps.asStateFlow()

    fun getUsers(): List<User> = _usersList

    fun getCurrentUser(): User {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!authUid.isNullOrBlank()) {
            val found = _usersList.find { it.id == authUid }
            if (found != null) return found
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val fallbackName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "Accra Member"
            val fallbackUser = User(
                id = authUid,
                name = fallbackName,
                email = firebaseUser?.email ?: "",
                location = "East Legon, Accra",
                skillsOffered = listOf("Skill Exchange"),
                skillsWanted = listOf("Mentorship"),
                rating = 5.0,
                ratingCount = 1,
                bio = "Active SkillSwap Accra member."
            )
            if (_usersList.none { it.id == authUid }) {
                _usersList = _usersList + fallbackUser
            }
            return fallbackUser
        }
        return _usersList.find { it.id == activeUserId } ?: _usersList.first()
    }

    fun setCurrentUser(userId: String) {
        activeUserId = userId
    }

    fun updateUserProfile(user: User) {
        if (user.id.isBlank()) return
        _usersList = _usersList.map { if (it.id == user.id) user else it }
        if (_usersList.none { it.id == user.id }) {
            _usersList = _usersList + user
        }
    }

    fun registerUser(user: User) {
        if (user.id.isBlank()) return
        if (_usersList.none { it.id == user.id }) {
            _usersList = _usersList + user
        }
        activeUserId = user.id
    }

    suspend fun sendSwapRequest(from: User, to: User): Swap {
        delay(1000)
        val swap = Swap(
            id = System.currentTimeMillis().toString(),
            fromUser = from,
            toUser = to,
            offeredSkill = from.skillsOffered.firstOrNull() ?: "General Mentorship",
            requestedSkill = to.skillsOffered.firstOrNull() ?: "General Mentorship",
            status = "pending"
        )
        _swaps.update { it + swap }
        return swap
    }

    suspend fun acceptRequest(swapId: String) {
        delay(800)
        _swaps.update { list ->
            list.map { if (it.id == swapId) it.copy(status = "accepted") else it }
        }
    }

    suspend fun declineRequest(swapId: String) {
        delay(800)
        _swaps.update { list ->
            list.map { if (it.id == swapId) it.copy(status = "declined") else it }
        }
    }
}
