package com.example.androidbenchmark

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Random

// 1. A Atividade Principal
class UiTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui_test)
        title = "Teste de UI (Lista Complexa)"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Gera 1.000 itens de dados falsos
        val data = (1..1000).map {
            ComplexItem(
                id = it.toLong(),
                title = "Item Título $it",
                description = "Descrição complexa para o item $it. " +
                        "Quanto mais texto, mais pesado é para renderizar. " +
                        "ID Único: ${System.nanoTime()}",
                color = Color.rgb(Random().nextInt(256), Random().nextInt(256), Random().nextInt(256))
            )
        }

        // Configura o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ComplexListAdapter(data)
    }
}

// 2. O Data Class para o item
data class ComplexItem(
    val id: Long,
    val title: String,
    val description: String,
    val color: Int
)

// 3. O Adapter para o RecyclerView
class ComplexListAdapter(private val items: List<ComplexItem>) :
    RecyclerView.Adapter<ComplexListAdapter.ComplexViewHolder>() {

    // Cria o ViewHolder (inflando o layout XML)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplexViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_complex, parent, false)
        return ComplexViewHolder(view)
    }

    // Retorna o número total de itens
    override fun getItemCount(): Int = items.size

    // Vincula os dados ao ViewHolder (chamado ao rolar)
    // Esta é a função crítica para o desempenho
    override fun onBindViewHolder(holder: ComplexViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    // 4. O ViewHolder (que segura as Views)
    class ComplexViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.item_title)
        private val description: TextView = itemView.findViewById(R.id.item_description)
        private val image: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(item: ComplexItem) {
            title.text = item.title
            description.text = item.description

            // Simula uma operação de "renderização" mais pesada
            // mudando a cor do ícone em vez de carregar uma imagem
            image.setColorFilter(item.color)
        }
    }
}