import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.transport_t.R

class Instr : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonNext: Button
    private lateinit var images: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instr)

        viewPager = findViewById(R.id.viewPager)
        buttonNext = findViewById(R.id.button_next)

        images = listOf(
            R.drawable.image1, // reemplaza con tus IDs de recursos de imagen
            R.drawable.image2,
            R.drawable.image3
        )

        val adapter = ImageAdapter(images)
        viewPager.adapter = adapter

        buttonNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < images.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else {

            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == images.size - 1) {
                    buttonNext.text = "Continuar"
                } else {
                    buttonNext.text = "Siguiente"
                }
            }
        })
    }
}
