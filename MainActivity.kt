import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import sevo.example.spygame.Fragment.TimerFragment
import sevo.example.spygame.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore
    private val wordList = mutableListOf<String>()
    private val selectedCategories = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCategory.setOnClickListener {
            openCategorySelection()
        }

        binding.btnVeri.setOnClickListener {
            displayRandomWordOrNotify()
        }

        binding.btnTime.setOnClickListener {
            showTimerDialog()
        }
    }

    private fun openCategorySelection() {
        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, 1001)
    }

    private fun displayRandomWordOrNotify() {
        if (wordList.isNotEmpty()) {
            displayRandomWord()
        } else {
            binding.txtWord.text = "No words available."
        }
    }

    private fun showTimerDialog() {
        val timerDialogFragment = TimerFragment()
        timerDialogFragment.show(supportFragmentManager, "timerDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val categories = data?.getIntegerArrayListExtra("selectedCategories")
            if (categories != null) {
                updateSelectedCategories(categories)
                fetchWordsFromCategories()
            }
        }
    }

    private fun updateSelectedCategories(categories: ArrayList<Int>) {
        selectedCategories.clear()
        selectedCategories.addAll(categories)
    }

    private fun fetchWordsFromCategories() {
        wordList.clear()
        for (categoryId in selectedCategories) {
            db.collection("Words")
                .whereEqualTo("CategoryID", categoryId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val wordName = document.getString("WordName")
                        wordName?.let { wordList.add(it) }
                    }
                    if (wordList.isNotEmpty()) {
                        displayRandomWord()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error fetching words: ", e)
                }
        }
    }

    private fun displayRandomWord() {
        val randomWord = wordList[Random.nextInt(wordList.size)]
        binding.txtWord.text = randomWord
    }
}
